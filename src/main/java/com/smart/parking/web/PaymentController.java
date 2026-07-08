package com.smart.parking.web;

import com.smart.parking.security.SecurityUtils;
import com.smart.parking.service.RazorpayService;
import com.smart.parking.web.dto.RazorpayOrderResponse;
import com.smart.parking.web.dto.VerifyPaymentRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final RazorpayService razorpayService;

    public PaymentController(RazorpayService razorpayService) {
        this.razorpayService = razorpayService;
    }

    /** Customer: create Razorpay order for a reservation */
    @PostMapping("/razorpay/create-order/{reservationId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RazorpayOrderResponse> createOrder(
            @PathVariable("reservationId") Long reservationId,
            HttpServletRequest request) {
        return ResponseEntity.ok(razorpayService.createReservationOrder(
                reservationId, SecurityUtils.currentUserId(), getIp(request)));
    }

    /** Customer: verify payment after Razorpay checkout */
    @PostMapping("/razorpay/verify")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Map<String,String>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest req,
            HttpServletRequest request) {
        razorpayService.verifyReservationPayment(req, SecurityUtils.currentUserId(), getIp(request));
        return ResponseEntity.ok(Map.of("status", "PAYMENT_VERIFIED", "message", "Payment confirmed!"));
    }

    /** Staff: create Razorpay order for gate UPI exit payment */
    @PostMapping("/razorpay/exit-order/{sessionId}")
    @PreAuthorize("hasAnyAuthority('MANAGER','SECURITY')")
    public ResponseEntity<RazorpayOrderResponse> createExitOrder(
            @PathVariable("sessionId") Long sessionId,
            HttpServletRequest request) {
        return ResponseEntity.ok(razorpayService.createExitOrder(
                sessionId, SecurityUtils.currentUserId(), getIp(request)));
    }

    /** Staff: verify UPI payment for gate exit */
    @PostMapping("/razorpay/verify-exit")
    @PreAuthorize("hasAnyAuthority('MANAGER','SECURITY')")
    public ResponseEntity<Map<String,String>> verifyExitPayment(
            @Valid @RequestBody VerifyPaymentRequest req,
            HttpServletRequest request) {
        razorpayService.verifyExitPayment(req, SecurityUtils.currentUserId(), getIp(request));
        return ResponseEntity.ok(Map.of("status", "PAYMENT_VERIFIED", "message", "Exit payment confirmed!"));
    }

    private String getIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip.split(",")[0].trim() : req.getRemoteAddr();
    }
}
