CREATE TABLE IF NOT EXISTS pricing_rules (
    id            BIGSERIAL PRIMARY KEY,
    space_type    VARCHAR(15)    NOT NULL
        CHECK (space_type IN ('REGULAR','COMPACT','EV','ACCESSIBLE')),
    vehicle_type  VARCHAR(10)    NOT NULL
        CHECK (vehicle_type IN ('CAR','BUS','BIKE','TRUCK','VAN')),
    rate_per_hour NUMERIC(8,2)   NOT NULL,
    minimum_hours INT            NOT NULL DEFAULT 1,
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    UNIQUE (space_type, vehicle_type)
);

-- Seed pricing rules (INR per hour)
INSERT INTO pricing_rules (space_type, vehicle_type, rate_per_hour, minimum_hours) VALUES
-- REGULAR
('REGULAR','CAR',   50.00, 1),
('REGULAR','BIKE',  20.00, 1),
('REGULAR','BUS',  120.00, 1),
('REGULAR','TRUCK', 100.00, 1),
('REGULAR','VAN',    70.00, 1),
-- COMPACT
('COMPACT','CAR',   40.00, 1),
('COMPACT','BIKE',  15.00, 1),
('COMPACT','BUS',   80.00, 1),
('COMPACT','TRUCK', 80.00, 1),
('COMPACT','VAN',   55.00, 1),
-- EV (includes charging premium)
('EV','CAR',        80.00, 1),
('EV','BIKE',       35.00, 1),
('EV','BUS',       150.00, 1),
('EV','TRUCK',     130.00, 1),
('EV','VAN',       100.00, 1),
-- ACCESSIBLE
('ACCESSIBLE','CAR',   40.00, 1),
('ACCESSIBLE','BIKE',  15.00, 1),
('ACCESSIBLE','BUS',  100.00, 1),
('ACCESSIBLE','TRUCK', 80.00, 1),
('ACCESSIBLE','VAN',   60.00, 1);
