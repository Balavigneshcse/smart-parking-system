CREATE TABLE IF NOT EXISTS reservations (
    id               BIGSERIAL PRIMARY KEY,
    reference        VARCHAR(20)    NOT NULL UNIQUE,
    vehicle_id       BIGINT         NOT NULL REFERENCES vehicles(id),
    space_id         BIGINT         NOT NULL REFERENCES parking_spaces(id),
    customer_user_id BIGINT         REFERENCES users(id),
    starts_at        TIMESTAMP      NOT NULL,
    ends_at          TIMESTAMP      NOT NULL,
    quoted_amount    NUMERIC(10,2),
    payment_mode     VARCHAR(15)
        CHECK (payment_mode IN ('ONLINE','ON_SPOT')),
    status           VARCHAR(20)    NOT NULL DEFAULT 'CONFIRMED'
        CHECK (status IN ('PENDING','CONFIRMED','CHECKED_IN','COMPLETED','CANCELLED','EXPIRED')),
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);
