CREATE TABLE IF NOT EXISTS parking_spaces (
    id         BIGSERIAL PRIMARY KEY,
    zone_id    BIGINT   NOT NULL REFERENCES parking_zones(id) ON DELETE CASCADE,
    code       VARCHAR(20) NOT NULL,
    space_type VARCHAR(15) NOT NULL
        CHECK (space_type IN ('REGULAR','COMPACT','EV','ACCESSIBLE')),
    status     VARCHAR(15) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE','OCCUPIED','RESERVED','MAINTENANCE'))
);

-- Seed spaces for every zone: 6 REGULAR, 2 COMPACT, 1 EV, 1 ACCESSIBLE
DO $$
DECLARE
    z RECORD;
    i INT;
BEGIN
    FOR z IN SELECT id FROM parking_zones LOOP
        FOR i IN 1..6 LOOP
            INSERT INTO parking_spaces (zone_id, code, space_type, status)
            VALUES (z.id, LPAD(i::TEXT,2,'0'), 'REGULAR', 'AVAILABLE');
        END LOOP;
        FOR i IN 7..8 LOOP
            INSERT INTO parking_spaces (zone_id, code, space_type, status)
            VALUES (z.id, LPAD(i::TEXT,2,'0'), 'COMPACT', 'AVAILABLE');
        END LOOP;
        INSERT INTO parking_spaces (zone_id, code, space_type, status)
        VALUES (z.id, '09', 'EV', 'AVAILABLE');
        INSERT INTO parking_spaces (zone_id, code, space_type, status)
        VALUES (z.id, '10', 'ACCESSIBLE', 'AVAILABLE');
    END LOOP;
END;
$$;
