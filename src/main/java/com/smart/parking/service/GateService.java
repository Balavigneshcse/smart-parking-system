package com.smart.parking.service;

import com.smart.parking.domain.*;
import com.smart.parking.domain.enums.*;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.exception.ResourceNotFoundException;
import com.smart.parking.repository.*;
import com.smart.parking.util.PlateNumberValidator;
import com.smart.parking.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GateService {

    private final VehicleRepository          vehicleRepo;
    private final VehicleSessionRepository   sessionRepo;
    private final ReservationRepository      reservationRepo;
    private final ParkingSpaceRepository     spaceRepo;
    private final PlaceStaffRepository       staffRepo;
    private final PaymentRepository          paymentRepo;
    private final FeeCalculator              feeCalculator;
    private final ActivityLogService         activityLog;
    private static final DateTimeFormatter   FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public GateService(VehicleRepository vehicleRepo, VehicleSessionRepository sessionRepo,
                       ReservationRepository reservationRepo, ParkingSpaceRepository spaceRepo,
                       PlaceStaffRepository staffRepo, PaymentRepository paymentRepo,
                       FeeCalculator feeCalculator, ActivityLogService activityLog) {
        this.vehicleRepo     = vehicleRepo;
        this.sessionRepo     = sessionRepo;
        this.reservationRepo = reservationRepo;
        this.spaceRepo       = spaceRepo;
        this.staffRepo       = staffRepo;
        this.paymentRepo     = paymentRepo;
        this.feeCalculator   = feeCalculator;
        this.activityLog     = activityLog;
    }

    // ── Vehicle lookup at gate ────────────────────────────────────────────────

    public VehicleLookupResponse lookup(String rawPlate) {
        String plate = PlateNumberValidator.normalise(rawPlate);
        var vehicleOpt = vehicleRepo.findByPlateNumber(plate);
        if (vehicleOpt.isEmpty()) {
            return new VehicleLookupResponse(false, false, false,
                    plate, null, null, null, null, null);
        }
        Vehicle v              = vehicleOpt.get();
        var activeSession      = sessionRepo.findByVehicle_PlateNumberAndStatus(plate, SessionStatus.ACTIVE);
        var activeReservation  = reservationRepo.findActiveReservationByPlate(plate, LocalDateTime.now());
        return new VehicleLookupResponse(
                true, activeSession.isPresent(), activeReservation.isPresent(),
                v.getPlateNumber(), v.getVehicleType().name(),
                v.getOwnerName(), v.getOwnerPhone(),
                activeSession.map(VehicleSession::getId).orElse(null),
                activeReservation.map(Reservation::getReference).orElse(null));
    }

    // ── Entry ─────────────────────────────────────────────────────────────────

    @Transactional
    public SessionResponse markEntry(EntryRequest req, User staffUser) {
        String plate = PlateNumberValidator.normalise(req.plateNumber());
        Vehicle vehicle = vehicleRepo.findByPlateNumber(plate)
                .orElseThrow(() -> new BusinessRuleException(
                        "Vehicle not found. Use on-spot registration."));

        if (sessionRepo.findByVehicle_PlateNumberAndStatus(plate, SessionStatus.ACTIVE).isPresent())
            throw new BusinessRuleException("Vehicle " + plate + " is already inside.");

        ParkingPlace  place       = getStaffPlace(staffUser);
        Reservation   reservation = reservationRepo
                .findActiveReservationByPlate(plate, LocalDateTime.now()).orElse(null);

        ParkingSpace space = null;
        if (reservation != null) {
            space = reservation.getSpace();
            reservation.setStatus(ReservationStatus.CHECKED_IN);
            reservationRepo.save(reservation);
        } else if (req.spaceId() != null) {
            space = spaceRepo.findById(req.spaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("ParkingSpace", req.spaceId()));
        }
        if (space != null) { space.setStatus(SpaceStatus.OCCUPIED); spaceRepo.save(space); }

        VehicleSession session = sessionRepo.save(VehicleSession.builder()
                .vehicle(vehicle).space(space).place(place).reservation(reservation)
                .entryTime(LocalDateTime.now()).entryBy(staffUser)
                .status(SessionStatus.ACTIVE).preBooked(reservation != null).build());

        activityLog.log(ActivityLogService.VEHICLE_ENTRY,
                "Entry: " + plate + " by " + staffUser.getName(),
                staffUser.getId(), staffUser.getName(), staffUser.getRole().name(), null, true);
        return toResponse(session);
    }

    @Transactional
    public SessionResponse onSpotEntry(OnSpotRegisterRequest req, User staffUser) {
        String plate = PlateNumberValidator.normalise(req.plateNumber());
        if (!PlateNumberValidator.isValid(plate))
            throw new BusinessRuleException("Invalid vehicle number. Format: TN09AB1234");
        if (sessionRepo.findByVehicle_PlateNumberAndStatus(plate, SessionStatus.ACTIVE).isPresent())
            throw new BusinessRuleException("Vehicle " + plate + " is already inside.");

        VehicleType vehicleType = VehicleType.valueOf(req.vehicleType().toUpperCase());
        Vehicle vehicle = vehicleRepo.findByPlateNumber(plate)
                .orElseGet(() -> vehicleRepo.save(Vehicle.builder()
                        .plateNumber(plate).vehicleType(vehicleType)
                        .ownerName(req.ownerName()).ownerPhone(req.ownerPhone())
                        .ownerEmail(req.ownerEmail()).build()));

        ParkingPlace place = getStaffPlace(staffUser);
        ParkingSpace space = null;
        if (req.spaceId() != null) {
            space = spaceRepo.findById(req.spaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("ParkingSpace", req.spaceId()));
            space.setStatus(SpaceStatus.OCCUPIED);
            spaceRepo.save(space);
        }

        VehicleSession session = sessionRepo.save(VehicleSession.builder()
                .vehicle(vehicle).space(space).place(place)
                .entryTime(LocalDateTime.now()).entryBy(staffUser)
                .status(SessionStatus.ACTIVE).preBooked(false).build());

        activityLog.log(ActivityLogService.ON_SPOT_ENTRY,
                "On-spot: " + plate, staffUser.getId(), staffUser.getName(),
                staffUser.getRole().name(), null, true);
        return toResponse(session);
    }

    // ── Session Details (NEW) — called by gate.html to decide exit flow ───────

    /**
     * Returns full session details including:
     * - paymentMode ("ONLINE" / "ON_SPOT" / null for walk-ins)
     * - alreadyPaidOnline — true if customer paid via Razorpay at booking time
     * - estimatedFee — live fee from entry to now (with overtime 2× if applicable)
     * - overtime — true if vehicle is past its booked end time
     *
     * The gate.html uses this to show either:
     *   (a) "Paid Online → just mark exit" button — when alreadyPaidOnline=true
     *   (b) Payment method selector → collect fee first — when alreadyPaidOnline=false
     */
    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetails(Long sessionId, User staffUser) {
        VehicleSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED)
            throw new BusinessRuleException("Session is already completed.");

        LocalDateTime now = LocalDateTime.now();
        SpaceType   spaceType   = session.getSpace() != null
                ? session.getSpace().getSpaceType() : SpaceType.REGULAR;
        VehicleType vehicleType = session.getVehicle().getVehicleType();

        // ── Determine payment mode and online-paid status ──────────────────────
        String  paymentMode      = null;
        boolean alreadyPaidOnline = false;
        String  reservationRef   = null;
        String  bookedUntil      = null;
        boolean overtime         = false;

        Reservation reservation = session.getReservation();
        if (reservation != null) {
            reservationRef = reservation.getReference();
            bookedUntil    = reservation.getEndsAt().format(FMT);
            overtime       = now.isAfter(reservation.getEndsAt());

            if (reservation.getPaymentMode() != null) {
                paymentMode = reservation.getPaymentMode().name();

                if (PaymentMode.ONLINE.name().equals(paymentMode)) {
                    // Check if a completed payment record exists for this reservation
                    alreadyPaidOnline = paymentRepo
                            .findByReservation_Id(reservation.getId())
                            .map(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED)
                            .orElse(false);
                }
            }
        }

        // ── Live fee estimate ──────────────────────────────────────────────────
        BigDecimal estimatedFee;
        if (reservation != null && overtime) {
            // Overtime: normal portion + 2× overtime portion
            BigDecimal normalFee = feeCalculator.calculate(
                    spaceType, vehicleType, session.getEntryTime(), reservation.getEndsAt());
            BigDecimal overtimeFee = feeCalculator.calculate(
                    spaceType, vehicleType, reservation.getEndsAt(), now)
                    .multiply(BigDecimal.valueOf(2));
            estimatedFee = normalFee.add(overtimeFee);
        } else {
            estimatedFee = feeCalculator.calculate(
                    spaceType, vehicleType, session.getEntryTime(), now);
        }

        String spaceName = session.getSpace() != null
                ? session.getSpace().getZone().getName() + "-" + session.getSpace().getCode()
                : "Walk-in";

        return new SessionDetailResponse(
                session.getId(),
                session.getVehicle().getPlateNumber(),
                session.getVehicle().getVehicleType().name(),
                session.getVehicle().getOwnerName(),
                spaceName,
                session.getEntryTime().format(FMT),
                session.getStatus().name(),
                session.isPreBooked(),
                reservationRef,
                paymentMode,
                alreadyPaidOnline,
                estimatedFee,
                bookedUntil,
                overtime);
    }

    // ── Exit ──────────────────────────────────────────────────────────────────

    /**
     * Marks a vehicle exit and records payment.
     *
     * Special paymentMethod value: "ONLINE_PREPAID"
     *   → Customer already paid online during booking.
     *   → We skip creating a new Payment record (it already exists for the reservation).
     *   → We still mark the session as COMPLETED and free the space.
     *
     * Overtime (vehicle past booked end time):
     *   Total fee = normalFee(entry → bookedEnd) + 2 × overtimeFee(bookedEnd → now)
     */
    @Transactional
    public SessionResponse markExit(Long sessionId, SessionExitRequest req, User staffUser) {
        VehicleSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));
        if (session.getStatus() == SessionStatus.COMPLETED)
            throw new BusinessRuleException("Session already completed.");

        LocalDateTime exitTime  = LocalDateTime.now();
        SpaceType   spaceType   = session.getSpace() != null
                ? session.getSpace().getSpaceType() : SpaceType.REGULAR;
        VehicleType vehicleType = session.getVehicle().getVehicleType();

        // ── Fee calculation with overtime 2× logic ─────────────────────────────
        BigDecimal fee;
        boolean overtime = false;
        Reservation reservation = session.getReservation();

        if (reservation != null && exitTime.isAfter(reservation.getEndsAt())) {
            overtime = true;
            BigDecimal normalFee = feeCalculator.calculate(
                    spaceType, vehicleType, session.getEntryTime(), reservation.getEndsAt());
            BigDecimal overtimeFee = feeCalculator.calculate(
                    spaceType, vehicleType, reservation.getEndsAt(), exitTime)
                    .multiply(BigDecimal.valueOf(2));
            fee = normalFee.add(overtimeFee);
        } else {
            fee = feeCalculator.calculate(spaceType, vehicleType, session.getEntryTime(), exitTime);
        }
        // ──────────────────────────────────────────────────────────────────────

        session.setExitTime(exitTime);
        session.setExitBy(staffUser);
        session.setStatus(SessionStatus.COMPLETED);
        session.setTotalFee(fee);
        sessionRepo.save(session);

        // Free up the space
        if (session.getSpace() != null) {
            session.getSpace().setStatus(SpaceStatus.AVAILABLE);
            spaceRepo.save(session.getSpace());
        }

        // ── ONLINE_PREPAID: customer already paid, skip new payment record ──────
        boolean isOnlinePrePaid = "ONLINE_PREPAID".equalsIgnoreCase(req.paymentMethod());
        if (!isOnlinePrePaid) {
            // Collect physical payment (CASH / UPI / CARD etc.)
            PaymentMethod method;
            try {
                method = PaymentMethod.valueOf(req.paymentMethod().toUpperCase());
            } catch (IllegalArgumentException e) {
                method = PaymentMethod.CASH; // safe fallback
            }
            paymentRepo.save(Payment.builder()
                    .session(session)
                    .amount(fee)
                    .paymentMethod(method)
                    .paymentStatus(PaymentStatus.COMPLETED)
                    .paidAt(exitTime)
                    .build());
        }
        // ──────────────────────────────────────────────────────────────────────

        String plate   = session.getVehicle().getPlateNumber();
        String logNote = isOnlinePrePaid
                ? "Exit for " + plate + " — pre-paid online ₹" + fee
                : overtime
                    ? "Exit for " + plate + " — ₹" + fee + " (includes 2× OVERTIME charge)"
                    : "Exit for " + plate + " — fee ₹" + fee;
        activityLog.logPayment(ActivityLogService.VEHICLE_EXIT, logNote,
                staffUser.getId(), staffUser.getName(), staffUser.getRole().name(),
                null, fee, null, true);

        return toResponse(session);
    }

    // ── Session lists ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SessionResponse> activeSessions(User staffUser) {
        return sessionRepo.findByPlace_IdAndStatus(
                        getStaffPlace(staffUser).getId(), SessionStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> todaySessions(User staffUser) {
        ParkingPlace place    = getStaffPlace(staffUser);
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return sessionRepo.findByPlaceAndDateRange(place.getId(), startOfDay, startOfDay.plusDays(1))
                .stream().map(this::toResponse).toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Looks up the Place a MANAGER or SECURITY staff is assigned to via PlaceStaff.
     * Throws BusinessRuleException (→ 400 via GlobalExceptionHandler) if no record found,
     * which gives a readable error message in the browser.
     */
    private ParkingPlace getStaffPlace(User staff) {
        return staffRepo.findActiveByUserId(staff.getId())
                .map(PlaceStaff::getPlace)
                .orElseThrow(() -> new BusinessRuleException(
                        "Staff '" + staff.getName() + "' is not assigned to any active parking place. " +
                        "Ask your Admin to assign you to a place."));
    }

    private SessionResponse toResponse(VehicleSession s) {
        String spaceName = s.getSpace() != null
                ? s.getSpace().getZone().getName() + "-" + s.getSpace().getCode() : "Walk-in";
        return new SessionResponse(s.getId(),
                s.getVehicle().getPlateNumber(), s.getVehicle().getVehicleType().name(),
                s.getVehicle().getOwnerName(), spaceName,
                s.getEntryTime() != null ? s.getEntryTime().format(FMT) : null,
                s.getExitTime()  != null ? s.getExitTime().format(FMT)  : null,
                s.getTotalFee(), s.getStatus().name(), s.isPreBooked());
    }
}
