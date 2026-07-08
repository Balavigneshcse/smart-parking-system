package com.smart.parking.service;

import com.smart.parking.config.AppProperties;
import com.smart.parking.domain.*;
import com.smart.parking.domain.enums.*;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.exception.ResourceNotFoundException;
import com.smart.parking.repository.*;
import com.smart.parking.web.dto.RazorpayOrderResponse;
import com.smart.parking.web.dto.VerifyPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Razorpay integration using Java's built-in HttpClient — no external SDK needed.
 * When app.razorpay.enabled=false (default), all methods work in mock/dev mode.
 * To go live: set enabled=true and add your real key-id + key-secret.
 */
@Service
public class RazorpayService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayService.class);
    private static final String RAZORPAY_ORDERS_URL = "https://api.razorpay.com/v1/orders";

    private final AppProperties props;
    private final ReservationRepository reservationRepo;
    private final VehicleSessionRepository sessionRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final PaymentRepository paymentRepo;
    private final FeeCalculator feeCalculator;
    private final ActivityLogService activityLog;

    public RazorpayService(AppProperties props,
                           ReservationRepository reservationRepo,
                           VehicleSessionRepository sessionRepo,
                           ParkingSpaceRepository spaceRepo,
                           PaymentRepository paymentRepo,
                           FeeCalculator feeCalculator,
                           ActivityLogService activityLog) {
        this.props = props;
        this.reservationRepo = reservationRepo;
        this.sessionRepo = sessionRepo;
        this.spaceRepo = spaceRepo;
        this.paymentRepo = paymentRepo;
        this.feeCalculator = feeCalculator;
        this.activityLog = activityLog;

        if (props.getRazorpay().isEnabled()) {
            log.info("✅ Razorpay enabled (live mode)");
        } else {
            log.info("ℹ️  Razorpay disabled — running in mock/dev mode. " +
                     "Set app.razorpay.enabled=true to activate real payments.");
        }
    }

    // ── Online reservation booking ─────────────────────────────────────────
    @Transactional
    public RazorpayOrderResponse createReservationOrder(Long reservationId, Long userId, String ip) {
        Reservation res = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        BigDecimal amount = res.getQuotedAmount();
        String razorpayOrderId = createOrder(amount, "Parking reservation " + res.getReference());

        activityLog.logPayment(ActivityLogService.PAYMENT_INITIATED,
                "Razorpay order for reservation " + res.getReference(),
                userId, null, "CUSTOMER", ip, amount, razorpayOrderId, true);

        return new RazorpayOrderResponse(
                razorpayOrderId, props.getRazorpay().getKeyId(),
                amount, props.getRazorpay().getCurrency(),
                res.getReference(), null,
                "Parking at " + res.getSpace().getZone().getPlace().getName());
    }

    @Transactional
    public void verifyReservationPayment(VerifyPaymentRequest req, Long userId, String ip) {
        verifySignature(req.razorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature());

        Reservation res = reservationRepo.findById(req.reservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", req.reservationId()));
        res.setStatus(ReservationStatus.CONFIRMED);
        reservationRepo.save(res);

        paymentRepo.save(Payment.builder()
                .reservation(res).amount(res.getQuotedAmount())
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionRef(req.razorpayPaymentId())
                .paidAt(LocalDateTime.now()).build());

        activityLog.logPayment(ActivityLogService.PAYMENT_SUCCESS,
                "Online payment confirmed for reservation " + res.getReference(),
                userId, null, "CUSTOMER", ip,
                res.getQuotedAmount(), req.razorpayPaymentId(), true);
    }

    // ── Gate exit via UPI/Razorpay ─────────────────────────────────────────
    @Transactional
    public RazorpayOrderResponse createExitOrder(Long sessionId, Long userId, String ip) {
        VehicleSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED)
            throw new BusinessRuleException("Session already completed.");

        SpaceType spaceType = session.getSpace() != null
                ? session.getSpace().getSpaceType() : SpaceType.REGULAR;
        BigDecimal amount = feeCalculator.calculate(
                spaceType, session.getVehicle().getVehicleType(),
                session.getEntryTime(), LocalDateTime.now());

        session.setTotalFee(amount);
        sessionRepo.save(session);

        String razorpayOrderId = createOrder(amount,
                "Parking exit — " + session.getVehicle().getPlateNumber());

        activityLog.logPayment(ActivityLogService.PAYMENT_INITIATED,
                "Razorpay UPI exit order for " + session.getVehicle().getPlateNumber(),
                userId, null, "STAFF", ip, amount, razorpayOrderId, true);

        return new RazorpayOrderResponse(
                razorpayOrderId, props.getRazorpay().getKeyId(),
                amount, props.getRazorpay().getCurrency(),
                null, sessionId,
                "Parking fee for " + session.getVehicle().getPlateNumber());
    }

    @Transactional
    public void verifyExitPayment(VerifyPaymentRequest req, Long userId, String ip) {
        verifySignature(req.razorpayOrderId(), req.razorpayPaymentId(), req.razorpaySignature());

        VehicleSession session = sessionRepo.findById(req.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session", req.sessionId()));

        if (session.getStatus() == SessionStatus.COMPLETED)
            throw new BusinessRuleException("Session already completed.");

        session.setStatus(SessionStatus.COMPLETED);
        session.setExitTime(LocalDateTime.now());
        sessionRepo.save(session);

        if (session.getSpace() != null) {
            session.getSpace().setStatus(SpaceStatus.AVAILABLE);
            spaceRepo.save(session.getSpace());
        }

        paymentRepo.save(Payment.builder()
                .session(session).amount(session.getTotalFee())
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionRef(req.razorpayPaymentId())
                .paidAt(LocalDateTime.now()).build());

        activityLog.logPayment(ActivityLogService.PAYMENT_SUCCESS,
                "UPI exit payment confirmed for " + session.getVehicle().getPlateNumber(),
                userId, null, "STAFF", ip,
                session.getTotalFee(), req.razorpayPaymentId(), true);
    }

    // ── Internal helpers — pure Java, zero external dependencies ──────────
    private String createOrder(BigDecimal amount, String receipt) {
        if (!props.getRazorpay().isEnabled()) {
            // Dev/mock mode — return a fake order ID so the app runs without real keys
            String mockId = "order_DEV_" + System.currentTimeMillis();
            log.info("Razorpay MOCK order created: {} for ₹{}", mockId, amount);
            return mockId;
        }
        try {
            // Amount in paise (1 INR = 100 paise)
            int paise = amount.multiply(BigDecimal.valueOf(100)).intValue();

            String body = "{\"amount\":" + paise
                    + ",\"currency\":\"" + props.getRazorpay().getCurrency() + "\""
                    + ",\"receipt\":\"" + receipt.replaceAll("[^a-zA-Z0-9_\\-]", "_").substring(0, Math.min(receipt.length(), 40)) + "\""
                    + "}";

            String credentials = Base64.getEncoder().encodeToString(
                    (props.getRazorpay().getKeyId() + ":" + props.getRazorpay().getKeySecret())
                            .getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RAZORPAY_ORDERS_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + credentials)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractJsonField(response.body(), "id");
            }
            throw new BusinessRuleException("Razorpay API error " + response.statusCode()
                    + ": " + response.body());

        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("Payment gateway error: " + e.getMessage());
        }
    }

    private void verifySignature(String orderId, String paymentId, String signature) {
        if (!props.getRazorpay().isEnabled()) {
            // Skip verification in dev/mock mode
            log.info("Razorpay disabled — skipping signature verification (dev mode)");
            return;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    props.getRazorpay().getKeySecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            String computed = HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            if (!computed.equals(signature)) {
                throw new BusinessRuleException("Payment verification failed — invalid signature.");
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("Signature check error: " + e.getMessage());
        }
    }

    /** Extract a string field from a JSON response without any JSON library */
    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int start = json.indexOf(key);
        if (start == -1) throw new BusinessRuleException("Field '" + field + "' not found in response");
        start += key.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
