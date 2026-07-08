CREATE TABLE IF NOT EXISTS parking_places (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(200)  NOT NULL,
    address      VARCHAR(500),
    category     VARCHAR(30)   NOT NULL
        CHECK (category IN ('AIRPORT','MALL','HOSPITAL','STADIUM','RAILWAY_STATION',
                            'BUS_TERMINUS','OFFICE_COMPLEX','HOTEL','TECH_PARK','OTHER')),
    state_id     BIGINT        NOT NULL REFERENCES states(id),
    total_spaces INT           NOT NULL DEFAULT 0,
    active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

ALTER TABLE users
    ADD CONSTRAINT fk_user_place  FOREIGN KEY (managed_place_id) REFERENCES parking_places(id),
    ADD CONSTRAINT fk_user_state  FOREIGN KEY (managed_state_id) REFERENCES states(id);

-- Seed Tamil Nadu airports
INSERT INTO parking_places (name, address, category, state_id, total_spaces, active) VALUES
(
  'Chennai International Airport',
  'Tirusulam, Chennai, Tamil Nadu 600027',
  'AIRPORT',
  (SELECT id FROM states WHERE code = 'TN'),
  80, true
),
(
  'Coimbatore International Airport',
  'Peelamedu, Coimbatore, Tamil Nadu 641014',
  'AIRPORT',
  (SELECT id FROM states WHERE code = 'TN'),
  60, true
),
(
  'Madurai Airport',
  'Avaniyapuram, Madurai, Tamil Nadu 625007',
  'AIRPORT',
  (SELECT id FROM states WHERE code = 'TN'),
  40, true
),
(
  'Tiruchirappalli International Airport',
  'Civil Aerodrome Post, Tiruchirappalli, Tamil Nadu 620007',
  'AIRPORT',
  (SELECT id FROM states WHERE code = 'TN'),
  40, true
);
