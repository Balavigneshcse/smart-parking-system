CREATE TABLE IF NOT EXISTS vehicle_sessions (
    id             BIGSERIAL PRIMARY KEY,
    vehicle_id     BIGINT      NOT NULL REFERENCES vehicles(id),
    space_id       BIGINT      REFERENCES parking_spaces(id),
    place_id       BIGINT      NOT NULL REFERENCES parking_places(id),
    reservation_id BIGINT      REFERENCES reservations(id),
    entry_time     TIMESTAMP   NOT NULL DEFAULT NOW(),
    exit_time      TIMESTAMP,
    entry_by_id    BIGINT      REFERENCES users(id),
    exit_by_id     BIGINT      REFERENCES users(id),
    total_fee      NUMERIC(10,2),
    status         VARCHAR(15) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE','COMPLETED')),
    is_pre_booked  BOOLEAN     NOT NULL DEFAULT FALSE
);
