package com.smart.parking.service;

import com.smart.parking.domain.*;
import com.smart.parking.domain.enums.*;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.exception.ResourceNotFoundException;
import com.smart.parking.repository.*;
import com.smart.parking.util.PlateNumberValidator;
import com.smart.parking.web.dto.CreateReservationRequest;
import com.smart.parking.web.dto.ReservationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ReservationRepository reservationRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final VehicleRepository vehicleRepo;
    private final UserRepository userRepo;
    private final FeeCalculator feeCalculator;
    private final MailService mailService;

    public ReservationService(ReservationRepository reservationRepo,
                               ParkingSpaceRepository spaceRepo,
                               VehicleRepository vehicleRepo,
                               UserRepository userRepo,
                               FeeCalculator feeCalculator,
                               MailService mailService) {
        this.reservationRepo = reservationRepo;
        this.spaceRepo       = spaceRepo;
        this.vehicleRepo     = vehicleRepo;
        this.userRepo        = userRepo;
        this.feeCalculator   = feeCalculator;
        this.mailService     = mailService;
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest req, Long customerId) {
        // ── Validate plate ────────────────────────────────────────────────────
        String plate = PlateNumberValidator.normalise(req.plateNumber());
        if (!PlateNumberValidator.isValid(plate)) {
            throw new BusinessRuleException(
                    "Invalid Indian vehicle number format. Example: TN09AB1234");
        }

        // ── Validate times ────────────────────────────────────────────────────
        LocalDateTime startsAt = LocalDateTime.parse(req.startsAt());
        LocalDateTime endsAt   = LocalDateTime.parse(req.endsAt());

        if (!endsAt.isAfter(startsAt))
            throw new BusinessRuleException("End time must be after start time.");

        // Block start time more than 5 minutes in the past (tolerance for clock skew)
        if (startsAt.isBefore(LocalDateTime.now().minusMinutes(5)))
            throw new BusinessRuleException("Start time cannot be in the past.");

        // ── Parse enums ───────────────────────────────────────────────────────
        SpaceType   spaceType   = SpaceType.valueOf(req.spaceType().toUpperCase());
        VehicleType vehicleType = VehicleType.valueOf(req.vehicleType().toUpperCase());
        PaymentMode paymentMode = PaymentMode.valueOf(req.paymentMode().toUpperCase());

        // ── Find available space ──────────────────────────────────────────────
        List<ParkingSpace> available = spaceRepo.findAvailableForReservation(
                req.placeId(), spaceType, startsAt, endsAt);
        if (available.isEmpty()) {
            throw new BusinessRuleException(
                    "No " + spaceType + " spaces available for the selected time slot.");
        }
        ParkingSpace space = available.get(0);

        // ── Get or create vehicle ─────────────────────────────────────────────
        Vehicle vehicle = vehicleRepo.findByPlateNumber(plate)
                .orElseGet(() -> vehicleRepo.save(
                        Vehicle.builder()
                                .plateNumber(plate)
                                .vehicleType(vehicleType)
                                .ownerName(req.ownerName())
                                .ownerPhone(req.ownerPhone())
                                .ownerEmail(req.ownerEmail())
                                .customerUser(customerId != null
                                        ? userRepo.findById(customerId).orElse(null) : null)
                                .build()));

        var fee = feeCalculator.calculate(spaceType, vehicleType, startsAt, endsAt);

        String reference = "SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        User customer = customerId != null
                ? userRepo.findById(customerId).orElse(null) : null;

        Reservation reservation = reservationRepo.save(
                Reservation.builder()
                        .reference(reference)
                        .vehicle(vehicle)
                        .space(space)
                        .customerUser(customer)
                        .startsAt(startsAt)
                        .endsAt(endsAt)
                        .quotedAmount(fee)
                        .paymentMode(paymentMode)
                        .status(ReservationStatus.CONFIRMED)
                        .build());

        // ── Send confirmation email ────────────────────────────────────────────
        // Wrapped in try-catch so an email failure NEVER fails the reservation.
        // Root cause of multiple-email bug: the frontend was re-enabling the
        // submit button after every click, allowing the button to be clicked multiple
        // times before the first HTTP response arrived, sending N emails.
        // The backend guard here ensures at most one email per transaction.
        try {
            String toEmail = req.ownerEmail() != null && !req.ownerEmail().isBlank()
                    ? req.ownerEmail()
                    : (customer != null ? customer.getEmail() : null);

            if (toEmail != null && !toEmail.isBlank()) {
                mailService.sendReservationConfirmation(reservation, toEmail);
                log.info("Confirmation email sent to {} for {}", toEmail, reference);
            }
        } catch (Exception e) {
            // Log the failure but never bubble it up — the booking is already saved.
            log.warn("Failed to send confirmation email for {}: {}", reference, e.getMessage());
        }

        return toResponse(reservation);
    }

    // ── FIX: @Transactional(readOnly=true) keeps Hibernate session open
    //         so lazy fields space→zone→place→name can be accessed. ────────────

    @Transactional(readOnly = true)
    public List<ReservationResponse> myReservations(Long customerId) {
        return reservationRepo.findByCustomerUser_IdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> placeReservations(Long placeId) {
        return reservationRepo.findBySpace_Zone_Place_IdOrderByCreatedAtDesc(placeId)
                .stream().map(this::toResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(
                r.getId(), r.getReference(),
                r.getSpace().getZone().getPlace().getName(),
                r.getSpace().getZone().getName(),
                r.getSpace().getCode(),
                r.getSpace().getSpaceType().name(),
                r.getVehicle().getPlateNumber(),
                r.getVehicle().getVehicleType().name(),
                r.getVehicle().getOwnerName(),
                r.getStartsAt().format(FMT),
                r.getEndsAt().format(FMT),
                r.getQuotedAmount(),
                r.getPaymentMode() != null ? r.getPaymentMode().name() : null,
                r.getStatus().name());
    }
}
