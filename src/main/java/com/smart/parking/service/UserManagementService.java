package com.smart.parking.service;

import com.smart.parking.domain.*;
import com.smart.parking.domain.enums.*;
import com.smart.parking.exception.BusinessRuleException;
import com.smart.parking.exception.ResourceNotFoundException;
import com.smart.parking.repository.*;
import com.smart.parking.util.PasswordValidator;
import com.smart.parking.web.dto.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserManagementService {

    private final UserRepository userRepo;
    private final PlaceStaffRepository staffRepo;
    private final ParkingPlaceRepository placeRepo;
    private final StateRepository stateRepo;
    private final PasswordEncoder encoder;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public UserManagementService(UserRepository userRepo, PlaceStaffRepository staffRepo,
                                  ParkingPlaceRepository placeRepo, StateRepository stateRepo,
                                  PasswordEncoder encoder) {
        this.userRepo  = userRepo;
        this.staffRepo = staffRepo;
        this.placeRepo = placeRepo;
        this.stateRepo = stateRepo;
        this.encoder   = encoder;
    }

    /** Super Admin creates STATE_MANAGER */
    @Transactional
    public UserResponse createStateManager(CreateUserRequest req) {
        validateNew(req);
        State state = stateRepo.findById(req.stateId())
                .orElseThrow(() -> new ResourceNotFoundException("State", req.stateId()));
        User u = buildUser(req, UserRole.STATE_MANAGER);
        u.setManagedState(state);
        return toResponse(userRepo.save(u));
    }

    /** Super Admin or State Manager creates ADMIN */
    @Transactional
    public UserResponse createAdmin(CreateUserRequest req, User creator) {
        validateNew(req);
        ParkingPlace place = placeRepo.findById(req.placeId())
                .orElseThrow(() -> new ResourceNotFoundException("ParkingPlace", req.placeId()));
        User u = buildUser(req, UserRole.ADMIN);
        u.setManagedPlace(place);
        return toResponse(userRepo.save(u));
    }

    /** Admin creates MANAGER or SECURITY staff for their place */
    @Transactional
    public StaffMemberResponse createStaff(AssignStaffRequest req, User adminUser) {
        if (userRepo.existsByEmail(req.email())) throw new BusinessRuleException("Email already registered.");
        if (!PasswordValidator.isStrong(req.password())) throw new BusinessRuleException(PasswordValidator.strengthMessage());

        ParkingPlace place = adminUser.getManagedPlace();
        if (place == null) throw new BusinessRuleException("Admin has no assigned place.");

        StaffRole staffRole = StaffRole.valueOf(req.staffRole().toUpperCase());
        UserRole  userRole  = staffRole == StaffRole.MANAGER ? UserRole.MANAGER : UserRole.SECURITY;

        User staff = userRepo.save(User.builder()
                .name(req.name()).email(req.email())
                .password(encoder.encode(req.password()))
                .phone(req.phone()).role(userRole).active(true)
                .createdAt(LocalDateTime.now()).build());

        staffRepo.save(PlaceStaff.builder()
                .user(staff).place(place).staffRole(staffRole)
                .assignedBy(adminUser).active(true).build());

        return new StaffMemberResponse(staff.getId(), staff.getName(), staff.getEmail(),
                staff.getPhone(), staffRole.name(), true);
    }

    @Transactional(readOnly = true)
    public List<StaffMemberResponse> listStaff(Long placeId) {
        return staffRepo.findByPlace_IdAndActiveTrue(placeId).stream()
                .map(ps -> new StaffMemberResponse(
                        ps.getUser().getId(), ps.getUser().getName(),
                        ps.getUser().getEmail(), ps.getUser().getPhone(),
                        ps.getStaffRole().name(), ps.isActive()))
                .toList();
    }

    @Transactional
    public void deactivateStaff(Long staffUserId, User adminUser) {
        PlaceStaff ps = staffRepo.findActiveByUserId(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffUserId));
        if (!ps.getPlace().getId().equals(adminUser.getManagedPlace().getId())) {
            throw new BusinessRuleException("Staff not in your place.");
        }
        ps.setActive(false);
        ps.getUser().setActive(false);
        staffRepo.save(ps);
        userRepo.save(ps.getUser());
    }

    // ── FIX: @Transactional(readOnly=true) keeps the session open so lazy
    //         getManagedPlace().getName() and getManagedState().getName() work ──

    @Transactional(readOnly = true)
    public List<UserResponse> listAllUsers() {
        return userRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAdmins() {
        return userRepo.findByRole(UserRole.ADMIN).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listStateManagers() {
        return userRepo.findByRole(UserRole.STATE_MANAGER).stream().map(this::toResponse).toList();
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void validateNew(CreateUserRequest req) {
        if (userRepo.existsByEmail(req.email())) throw new BusinessRuleException("Email already registered.");
        if (!PasswordValidator.isStrong(req.password())) throw new BusinessRuleException(PasswordValidator.strengthMessage());
    }

    private User buildUser(CreateUserRequest req, UserRole role) {
        return User.builder()
                .name(req.name()).email(req.email())
                .password(encoder.encode(req.password()))
                .phone(req.phone()).role(role).active(true)
                .createdAt(LocalDateTime.now()).build();
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getRole().name(),
                u.getManagedPlace() != null ? u.getManagedPlace().getName() : null,
                u.getManagedState() != null ? u.getManagedState().getName() : null,
                u.isActive(),
                u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : null);
    }
}
