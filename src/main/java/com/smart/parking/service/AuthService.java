package com.smart.parking.service;

import com.smart.parking.domain.User;
import com.smart.parking.domain.enums.UserRole;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.repository.UserRepository;
import com.smart.parking.security.JwtService;
import com.smart.parking.util.PasswordValidator;
import com.smart.parking.web.dto.AuthResponse;
import com.smart.parking.web.dto.LoginRequest;
import com.smart.parking.web.dto.RegisterRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final ActivityLogService activityLog;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder,
                       JwtService jwtService, OtpService otpService,
                       ActivityLogService activityLog) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.activityLog = activityLog;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req, String ip) {
        if (userRepo.existsByEmail(req.email()))
            throw new BusinessRuleException("Email already registered.");
        if (!PasswordValidator.isStrong(req.password()))
            throw new BusinessRuleException(PasswordValidator.strengthMessage());

        User user = userRepo.save(User.builder()
                .name(req.name()).email(req.email())
                .password(encoder.encode(req.password()))
                .phone(req.phone()).role(UserRole.CUSTOMER)
                .dateOfBirth(LocalDate.parse(req.dateOfBirth()))
                .createdAt(LocalDateTime.now()).active(true).build());

        activityLog.log(ActivityLogService.USER_REGISTER,
                "New customer registered: " + req.email(),
                user.getId(), user.getName(), "CUSTOMER", ip, true);

        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest req, String ip) {
        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> {
                    activityLog.logAnonymous(ActivityLogService.USER_LOGIN,
                            "Login failed — email not found: " + req.email(), ip, false);
                    return new BadCredentialsException("Invalid credentials.");
                });
        if (!user.isActive()) {
            activityLog.log(ActivityLogService.USER_LOGIN, "Login failed — account inactive",
                    user.getId(), user.getName(), user.getRole().name(), ip, false);
            throw new BusinessRuleException("Account is deactivated.");
        }
        if (!encoder.matches(req.password(), user.getPassword())) {
            activityLog.log(ActivityLogService.USER_LOGIN, "Login failed — wrong password",
                    user.getId(), user.getName(), user.getRole().name(), ip, false);
            throw new BadCredentialsException("Invalid credentials.");
        }
        activityLog.log(ActivityLogService.USER_LOGIN, "Login successful",
                user.getId(), user.getName(), user.getRole().name(), ip, true);
        return buildResponse(user);
    }

    /** Step 1 — request OTP */
    public void forgotPassword(String email, String ip) {
        otpService.sendOtp(email);
        activityLog.logAnonymous(ActivityLogService.PASSWORD_RESET_REQUEST,
                "OTP requested for: " + email, ip, true);
    }

    /** Step 2 — verify OTP only (returns ok or throws) */
    public void verifyOtp(String email, String otp) {
        otpService.verifyOtp(email, otp);
    }

    /** Step 3 — reset password */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword, String ip) {
        if (!PasswordValidator.isStrong(newPassword))
            throw new BusinessRuleException(PasswordValidator.strengthMessage());

        otpService.verifyAndReset(email, otp, newPassword);   // validates + marks used

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found."));
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);

        activityLog.log(ActivityLogService.PASSWORD_RESET_COMPLETE,
                "Password reset successfully for " + email,
                user.getId(), user.getName(), user.getRole().name(), ip, true);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user);
        Long placeId = user.getManagedPlace() != null ? user.getManagedPlace().getId() : null;
        Long stateId = user.getManagedState() != null ? user.getManagedState().getId() : null;
        return new AuthResponse(token, user.getRole().name(), user.getId(),
                user.getName(), user.getEmail(), placeId, stateId);
    }
}
