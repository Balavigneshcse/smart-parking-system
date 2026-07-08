package com.smart.parking.web;

import com.smart.parking.service.AuthService;
import com.smart.parking.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req,
                                                 HttpServletRequest request) {
        return ResponseEntity.ok(authService.register(req, getIp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletRequest request) {
        return ResponseEntity.ok(authService.login(req, getIp(request)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String,String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req,
            HttpServletRequest request) {
        authService.forgotPassword(req.email(), getIp(request));
        return ResponseEntity.ok(Map.of("message",
                "OTP sent to your email. Valid for 10 minutes."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String,String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest req) {
        authService.verifyOtp(req.email(), req.otp());
        return ResponseEntity.ok(Map.of("message", "OTP verified. Proceed to reset password."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String,String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req,
            HttpServletRequest request) {
        authService.resetPassword(req.email(), req.otp(), req.newPassword(), getIp(request));
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. Please login."));
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : req.getRemoteAddr();
    }
}
