#  Smart Parking System

> Full-stack intelligent parking management — Spring Boot 4.1.0 · PostgreSQL · Royal Purple UI

---

##  Quick Start (5 steps

### 1 — Prerequisites
| Tool | Version | Download |
|------|---------|----------|
| JDK | 21 LTS | https://adoptium.net |
| Maven | 3.9+ | bundled via `mvnw` |
| PostgreSQL | 16+ | https://www.postgresql.org |
| IntelliJ IDEA | Any | https://www.jetbrains.com/idea |

### 2 — Create the database
Open **pgAdmin 4** or run in terminal:
```sql
CREATE DATABASE smartparking;
```

### 3 — Configure credentials
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartparking
    username: postgres          # ← your PG username
    password: postgres          # ← your PG password
```

### 4 — Import in IntelliJ
1. Open IntelliJ → **File → Open** → select project folder
2. Right-click `pom.xml` → **"Add as Maven Project"**
3. **File → Project Structure → SDKs** → Add JDK 21
4. Wait for Maven to download dependencies (~2 min first time)

### 5 — Run
Right-click `SmartParkingApplication.java` → **Run**

Open browser: **http://localhost:8080**

---

##  Default Credentials

| Role | Email | Password | Portal |
|------|-------|----------|--------|
| Super Admin | superadmin@smartparking.com | SuperAdmin@123 | /super/login.html |

> All other users are created via the Super Admin portal.

---

##  Architecture

```
com.smart.parking
├── config/          AppConfig, AppProperties, SecurityConfig, SuperAdminSeeder
├── domain/          12 JPA entities + 11 enums
├── repository/      11 Spring Data JPA repositories
├── security/        JWT (JJWT 0.12.6), AuthUser, JwtAuthFilter
├── service/         AuthService, MailService*, ReservationService,
│                    GateService, StatsService, UserManagementService,
│                    PlaceService, FeeCalculator
├── web/             9 REST controllers + GlobalExceptionHandler
│   └── dto/         20 Java record DTOs
├── exception/       BusinessRuleException, ResourceNotFoundException
└── util/            PlateNumberValidator, PasswordValidator
```

*MailService logic is preserved verbatim from the original v2 source.*

---

##  Role Hierarchy

```
SUPER_ADMIN  →  creates State Managers & Admins, sees all stats
STATE_MANAGER → creates Admins for their state
ADMIN        →  manages one place, creates Manager/Security staff
MANAGER      →  gate entry/exit + vehicle lookup
SECURITY     →  gate entry/exit + vehicle lookup
CUSTOMER     →  register, book, view reservations
```

---

##  Frontend Pages

| URL | Page |
|-----|------|
| / | Landing page |
| /customer/login.html | Customer login |
| /customer/register.html | Customer registration |
| /customer/dashboard.html | Choose place, live stats |
| /customer/occupancy.html?placeId=1 | Live parking map |
| /customer/book.html | 3-step booking wizard |
| /customer/my-reservations.html | Booking history |
| /admin/login.html | Admin login |
| /admin/dashboard.html | Stats + staff management |
| /staff/login.html | Staff login (Manager/Security) |
| /staff/gate.html | Gate control panel |
| /super/login.html | Super Admin login |
| /super/dashboard.html | System overview |
| /state/login.html | State Manager login |
| /state/dashboard.html | State-level management |

---

##  REST API Summary

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | /api/auth/register | Public |
| POST | /api/auth/login | Public |
| GET | /api/places | Public |
| GET | /api/places/{id}/occupancy | Public |
| POST | /api/reservations | CUSTOMER |
| GET | /api/reservations/mine | CUSTOMER |
| GET | /api/gate/check/{plate} | MANAGER/SECURITY |
| POST | /api/gate/entry | MANAGER/SECURITY |
| POST | /api/gate/entry/on-spot | MANAGER/SECURITY |
| POST | /api/gate/exit/{sessionId} | MANAGER/SECURITY |
| GET | /api/gate/sessions/active | MANAGER/SECURITY |
| GET | /api/gate/sessions/today | MANAGER/SECURITY |
| GET | /api/admin/stats/daily | ADMIN |
| GET | /api/admin/stats/weekly | ADMIN |
| GET | /api/admin/stats/monthly | ADMIN |
| GET | /api/admin/staff | ADMIN |
| POST | /api/admin/staff | ADMIN |
| DELETE | /api/admin/staff/{id} | ADMIN |
| POST | /api/super/state-managers | SUPER_ADMIN |
| POST | /api/super/admins | SUPER_ADMIN |
| GET | /api/super/stats | SUPER_ADMIN |
| GET | /api/super/users | SUPER_ADMIN |
| POST | /api/state/admins | STATE_MANAGER |

Full collection: import `SmartParking.postman_collection.json` into Postman.

---

##  Database Schema

12 tables managed by **Flyway** (VARCHAR + CHECK constraints — no native ENUMs):

`users` · `states` · `parking_places` · `parking_zones` · `parking_spaces`
`vehicles` · `reservations` · `vehicle_sessions` · `payments`
`place_staff` · `pricing_rules`

---

##  Seed Data

On first startup, Flyway runs 12 migration scripts automatically:
- **4 Tamil Nadu airports** seeded (Chennai, Coimbatore, Madurai, Tiruchirappalli)
- **8 zones** (2 per airport) with **80 spaces** total
- **20 pricing rules** (5 vehicle types × 4 space types)
- **Super Admin** seeded by `SuperAdminSeeder` bean on startup

---

##  Indian Vehicle Plate Validation

Format: `STATE_CODE + DISTRICT_NUM + SERIES + NUMBER`
Example: `TN09AB1234`
Regex: `^[A-Z]{2}\d{2}[A-Z]{1,3}\d{1,4}$`

---

##  Email Configuration

To enable reservation confirmation emails, edit `application.yml`:
```yaml
app:
  mail:
    enabled: true              # ← change to true
    from: noreply@yourdomain.com
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password   # Gmail App Password
```

---

##  Fee Calculation

Fee = `rate_per_hour × ceil(duration_hours)`  (minimum 1 hour)

| Space Type | CAR | BIKE | BUS | TRUCK | VAN |
|------------|-----|------|-----|-------|-----|
| REGULAR | ₹50/hr | ₹20/hr | ₹120/hr | ₹100/hr | ₹70/hr |
| COMPACT | ₹40/hr | ₹15/hr | ₹80/hr | ₹80/hr | ₹55/hr |
| EV | ₹80/hr | ₹35/hr | ₹150/hr | ₹130/hr | ₹100/hr |
| ACCESSIBLE | ₹40/hr | ₹15/hr | ₹100/hr | ₹80/hr | ₹60/hr |

---

##  Design System

| Token | Value | Usage |
|-------|-------|-------|
| `--bg` | `#0F0A2E` | Page background |
| `--surface` | `#1A1535` | Cards |
| `--accent` | `#7C3AED` | Buttons, highlights |
| `--sky` | `#38BDF8` | Secondary accent |
| `--success` | `#10B981` | Available spaces |
| `--danger` | `#EF4444` | Occupied, errors |

---

*Built with  · Spring Boot 4.1.0 · JDK 21 · PostgreSQL · Flyway*
