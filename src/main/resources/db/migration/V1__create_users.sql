CREATE TABLE IF NOT EXISTS users (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(150)        NOT NULL,
    email            VARCHAR(200)        NOT NULL UNIQUE,
    password         VARCHAR(255)        NOT NULL,
    role             VARCHAR(20)         NOT NULL
        CHECK (role IN ('SUPER_ADMIN','STATE_MANAGER','ADMIN','MANAGER','SECURITY','CUSTOMER')),
    phone            VARCHAR(15),
    date_of_birth    DATE,
    created_at       TIMESTAMP           NOT NULL DEFAULT NOW(),
    active           BOOLEAN             NOT NULL DEFAULT TRUE,
    managed_place_id BIGINT,
    managed_state_id BIGINT
);
