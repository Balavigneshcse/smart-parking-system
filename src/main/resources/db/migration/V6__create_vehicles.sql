CREATE TABLE IF NOT EXISTS vehicles (
    id               BIGSERIAL PRIMARY KEY,
    plate_number     VARCHAR(20)  NOT NULL,
    vehicle_type     VARCHAR(10)  NOT NULL
        CHECK (vehicle_type IN ('CAR','BUS','BIKE','TRUCK','VAN')),
    owner_name       VARCHAR(150) NOT NULL,
    owner_phone      VARCHAR(15),
    owner_email      VARCHAR(200),
    customer_user_id BIGINT REFERENCES users(id),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);
