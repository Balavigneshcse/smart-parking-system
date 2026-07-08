package com.smart.parking.config;

import com.smart.parking.domain.*;
import com.smart.parking.domain.enums.*;
import com.smart.parking.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds reference data on first startup.
 * ORDER 2 — runs after SuperAdminSeeder.
 *
 * Seeds:
 *  - 10 Indian states
 *  - 4 Tamil Nadu airports with zones and spaces
 *  - 20 pricing rules (4 space types × 5 vehicle types)
 *  - Demo staff for Chennai Airport (ADMIN, MANAGER, SECURITY) for immediate testing
 */
@Component
@Order(2)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StateRepository       stateRepo;
    private final ParkingPlaceRepository placeRepo;
    private final ParkingZoneRepository  zoneRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final PricingRuleRepository  pricingRepo;
    private final UserRepository         userRepo;
    private final PlaceStaffRepository   staffRepo;
    private final PasswordEncoder        encoder;

    public DataSeeder(StateRepository stateRepo, ParkingPlaceRepository placeRepo,
                      ParkingZoneRepository zoneRepo, ParkingSpaceRepository spaceRepo,
                      PricingRuleRepository pricingRepo, UserRepository userRepo,
                      PlaceStaffRepository staffRepo, PasswordEncoder encoder) {
        this.stateRepo  = stateRepo;
        this.placeRepo  = placeRepo;
        this.zoneRepo   = zoneRepo;
        this.spaceRepo  = spaceRepo;
        this.pricingRepo = pricingRepo;
        this.userRepo   = userRepo;
        this.staffRepo  = staffRepo;
        this.encoder    = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            seedStates();
            seedPlaces();
            seedPricingRules();
            seedDemoUsers();      // ← seeds test staff so gate works immediately
            log.info("✅ Data seeding complete.");
        } catch (Exception e) {
            log.error("❌ DataSeeder failed: {}", e.getMessage(), e);
        }
    }

    // ── States ────────────────────────────────────────────────────────────────
    private void seedStates() {
        if (stateRepo.count() > 0) { log.info("ℹ️  States already seeded."); return; }
        List<String[]> states = List.of(
            new String[]{"Tamil Nadu",    "TN"},
            new String[]{"Maharashtra",   "MH"},
            new String[]{"Karnataka",     "KA"},
            new String[]{"Telangana",     "TS"},
            new String[]{"Delhi",         "DL"},
            new String[]{"Gujarat",       "GJ"},
            new String[]{"West Bengal",   "WB"},
            new String[]{"Rajasthan",     "RJ"},
            new String[]{"Uttar Pradesh", "UP"},
            new String[]{"Kerala",        "KL"}
        );
        states.forEach(s -> stateRepo.save(State.builder().name(s[0]).code(s[1]).build()));
        log.info("✅ Seeded {} states.", states.size());
    }

    // ── Tamil Nadu Airport Places ─────────────────────────────────────────────
    private void seedPlaces() {
        if (placeRepo.count() > 0) { log.info("ℹ️  Places already seeded."); return; }
        State tn = stateRepo.findByCode("TN")
                .orElseThrow(() -> new IllegalStateException("TN state not found"));

        record AirportDef(String name, String address, String zA, String aDesc, String zB, String bDesc) {}
        List<AirportDef> airports = List.of(
            new AirportDef(
                "Chennai International Airport",
                "Tirusulam, Chennai, Tamil Nadu 600027",
                "Zone A", "Ground Level – Terminal 1",
                "Zone B", "First Floor – Terminal 2"),
            new AirportDef(
                "Coimbatore International Airport",
                "Peelamedu, Coimbatore, Tamil Nadu 641014",
                "Zone A", "Ground Level",
                "Zone B", "First Floor"),
            new AirportDef(
                "Madurai Airport",
                "Avaniyapuram, Madurai, Tamil Nadu 625007",
                "Zone A", "Ground Level",
                "Zone B", "First Floor"),
            new AirportDef(
                "Tiruchirappalli International Airport",
                "Civil Aerodrome Post, Tiruchirappalli, Tamil Nadu 620007",
                "Zone A", "Ground Level",
                "Zone B", "First Floor")
        );

        for (AirportDef ap : airports) {
            ParkingPlace place = placeRepo.save(ParkingPlace.builder()
                    .name(ap.name()).address(ap.address())
                    .category(PlaceCategory.AIRPORT).state(tn)
                    .totalSpaces(20).active(true).build());

            ParkingZone zA = zoneRepo.save(ParkingZone.builder()
                    .place(place).name(ap.zA()).level("G").description(ap.aDesc()).build());
            ParkingZone zB = zoneRepo.save(ParkingZone.builder()
                    .place(place).name(ap.zB()).level("1").description(ap.bDesc()).build());

            seedSpacesForZone(zA);
            seedSpacesForZone(zB);
            log.info("✅  Seeded place: {}", ap.name());
        }
    }

    private void seedSpacesForZone(ParkingZone zone) {
        record SpaceDef(String code, SpaceType type) {}
        List<SpaceDef> defs = List.of(
            new SpaceDef("01", SpaceType.REGULAR),
            new SpaceDef("02", SpaceType.REGULAR),
            new SpaceDef("03", SpaceType.REGULAR),
            new SpaceDef("04", SpaceType.REGULAR),
            new SpaceDef("05", SpaceType.REGULAR),
            new SpaceDef("06", SpaceType.REGULAR),
            new SpaceDef("07", SpaceType.COMPACT),
            new SpaceDef("08", SpaceType.COMPACT),
            new SpaceDef("09", SpaceType.EV),
            new SpaceDef("10", SpaceType.ACCESSIBLE)
        );
        for (SpaceDef d : defs) {
            spaceRepo.save(ParkingSpace.builder()
                    .zone(zone).code(d.code())
                    .spaceType(d.type()).status(SpaceStatus.AVAILABLE).build());
        }
    }

    // ── Pricing Rules ─────────────────────────────────────────────────────────
    private void seedPricingRules() {
        if (pricingRepo.count() > 0) { log.info("ℹ️  Pricing rules already seeded."); return; }
        record Rule(SpaceType st, VehicleType vt, String rate) {}
        List<Rule> rules = List.of(
            new Rule(SpaceType.REGULAR,    VehicleType.CAR,   "50.00"),
            new Rule(SpaceType.REGULAR,    VehicleType.BIKE,  "20.00"),
            new Rule(SpaceType.REGULAR,    VehicleType.BUS,  "120.00"),
            new Rule(SpaceType.REGULAR,    VehicleType.TRUCK,"100.00"),
            new Rule(SpaceType.REGULAR,    VehicleType.VAN,   "70.00"),
            new Rule(SpaceType.COMPACT,    VehicleType.CAR,   "40.00"),
            new Rule(SpaceType.COMPACT,    VehicleType.BIKE,  "15.00"),
            new Rule(SpaceType.COMPACT,    VehicleType.BUS,   "80.00"),
            new Rule(SpaceType.COMPACT,    VehicleType.TRUCK, "80.00"),
            new Rule(SpaceType.COMPACT,    VehicleType.VAN,   "55.00"),
            new Rule(SpaceType.EV,         VehicleType.CAR,   "80.00"),
            new Rule(SpaceType.EV,         VehicleType.BIKE,  "35.00"),
            new Rule(SpaceType.EV,         VehicleType.BUS,  "150.00"),
            new Rule(SpaceType.EV,         VehicleType.TRUCK,"130.00"),
            new Rule(SpaceType.EV,         VehicleType.VAN,  "100.00"),
            new Rule(SpaceType.ACCESSIBLE, VehicleType.CAR,   "40.00"),
            new Rule(SpaceType.ACCESSIBLE, VehicleType.BIKE,  "15.00"),
            new Rule(SpaceType.ACCESSIBLE, VehicleType.BUS,  "100.00"),
            new Rule(SpaceType.ACCESSIBLE, VehicleType.TRUCK, "80.00"),
            new Rule(SpaceType.ACCESSIBLE, VehicleType.VAN,   "60.00")
        );
        rules.forEach(r -> pricingRepo.save(PricingRule.builder()
                .spaceType(r.st()).vehicleType(r.vt())
                .ratePerHour(new BigDecimal(r.rate()))
                .minimumHours(1).active(true).build()));
        log.info("✅ Seeded {} pricing rules.", rules.size());
    }

    // ── Demo Staff Users (ONE-TIME) ───────────────────────────────────────────
    // Creates STATE MANAGER, ADMIN, MANAGER, SECURITY for Chennai Airport.
    // These are used for immediate testing without needing manual user creation.
    // Skipped if already seeded.
    private void seedDemoUsers() {
        if (userRepo.existsByEmail("admin@csia.in")) {
            log.info("ℹ️  Demo staff users already seeded.");
            return;
        }

        State tn = stateRepo.findByCode("TN").orElse(null);
        if (tn == null) { log.warn("⚠️  TN state missing — skipping demo user seed."); return; }

        ParkingPlace chennai = placeRepo.findAll().stream()
                .filter(p -> p.getName().contains("Chennai"))
                .findFirst().orElse(null);
        if (chennai == null) { log.warn("⚠️  Chennai place missing — skipping demo user seed."); return; }

        // ── STATE MANAGER for Tamil Nadu ──────────────────────────────────────
        userRepo.save(User.builder()
                .name("TN State Manager")
                .email("statemanager@smartpark.in")
                .password(encoder.encode("Manager@1234"))
                .phone("9100000001")
                .role(UserRole.STATE_MANAGER)
                .managedState(tn)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        // ── ADMIN for Chennai Airport ─────────────────────────────────────────
        User admin = userRepo.save(User.builder()
                .name("Chennai Airport Admin")
                .email("admin@csia.in")
                .password(encoder.encode("Admin@1234"))
                .phone("9100000002")
                .role(UserRole.ADMIN)
                .managedPlace(chennai)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());

        // ── MANAGER for Chennai Airport (via PlaceStaff) ──────────────────────
        User manager = userRepo.save(User.builder()
                .name("Chennai Airport Manager")
                .email("manager@csia.in")
                .password(encoder.encode("Staff@1234"))
                .phone("9100000003")
                .role(UserRole.MANAGER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());
        staffRepo.save(PlaceStaff.builder()
                .user(manager).place(chennai)
                .staffRole(StaffRole.MANAGER)
                .assignedBy(admin).active(true).build());

        // ── SECURITY for Chennai Airport (via PlaceStaff) ─────────────────────
        User security = userRepo.save(User.builder()
                .name("Gate Security")
                .email("security@csia.in")
                .password(encoder.encode("Staff@1234"))
                .phone("9100000004")
                .role(UserRole.SECURITY)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());
        staffRepo.save(PlaceStaff.builder()
                .user(security).place(chennai)
                .staffRole(StaffRole.SECURITY)
                .assignedBy(admin).active(true).build());

        log.info("✅ Demo staff seeded for Chennai Airport:");
        log.info("   State Manager : statemanager@smartpark.in / Manager@1234");
        log.info("   Admin         : admin@csia.in        / Admin@1234");
        log.info("   Manager (gate): manager@csia.in      / Staff@1234");
        log.info("   Security      : security@csia.in     / Staff@1234");
    }
}
