CREATE TABLE IF NOT EXISTS place_staff (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users(id),
    place_id       BIGINT      NOT NULL REFERENCES parking_places(id),
    staff_role     VARCHAR(15) NOT NULL
        CHECK (staff_role IN ('MANAGER','SECURITY')),
    assigned_by_id BIGINT      REFERENCES users(id),
    assigned_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    active         BOOLEAN     NOT NULL DEFAULT TRUE
);
