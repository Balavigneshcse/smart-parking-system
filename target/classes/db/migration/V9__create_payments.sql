CREATE TABLE IF NOT EXISTS payments (
    id             BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT         REFERENCES reservations(id),
    session_id     BIGINT         REFERENCES vehicle_sessions(id),
    amount         NUMERIC(10,2)  NOT NULL,
    payment_method VARCHAR(20)
        CHECK (payment_method IN ('CASH','UPI','CARD','NET_BANKING','WALLET')),
    payment_status VARCHAR(15)    NOT NULL DEFAULT 'PENDING'
        CHECK (payment_status IN ('PENDING','COMPLETED','FAILED','REFUNDED')),
    transaction_ref VARCHAR(100),
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);
