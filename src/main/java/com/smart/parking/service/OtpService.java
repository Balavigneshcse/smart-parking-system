package com.smart.parking.service;

import com.smart.parking.domain.PasswordResetOtp;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.exception.ResourceNotFoundException;
import com.smart.parking.repository.PasswordResetOtpRepository;
import com.smart.parking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private static final int OTP_VALIDITY_MINUTES = 10;
    private final SecureRandom random = new SecureRandom();

    private final PasswordResetOtpRepository otpRepo;
    private final UserRepository userRepo;
    private final OtpMailService otpMailService;

    public OtpService(PasswordResetOtpRepository otpRepo, UserRepository userRepo,
                      OtpMailService otpMailService) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.otpMailService = otpMailService;
    }

    @Transactional
    public void sendOtp(String email) {
        // Check email exists
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Invalidate old OTPs
        otpRepo.invalidateAllForEmail(email);

        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1_000_000));

        otpRepo.save(PasswordResetOtp.builder()
                .email(email).otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .createdAt(LocalDateTime.now())
                .used(false).build());

        otpMailService.sendOtp(email, otp, user.getName());
    }

    @Transactional
    public void verifyAndReset(String email, String otp, String newPassword) {
        PasswordResetOtp record = otpRepo
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessRuleException("No OTP found. Please request a new one."));

        if (record.isExpired()) {
            throw new BusinessRuleException("OTP has expired. Please request a new one.");
        }
        if (!record.getOtp().equals(otp)) {
            throw new BusinessRuleException("Invalid OTP. Please check and try again.");
        }
        record.setUsed(true);
        otpRepo.save(record);
    }

    /** Verify OTP only (step 2 of 3-step flow) */
    public void verifyOtp(String email, String otp) {
        PasswordResetOtp record = otpRepo
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BusinessRuleException("No OTP found. Please request a new one."));
        if (record.isExpired()) throw new BusinessRuleException("OTP expired. Request a new one.");
        if (!record.getOtp().equals(otp)) throw new BusinessRuleException("Invalid OTP.");
    }
}
