CREATE TABLE IF NOT EXISTS parking_zones (
    id          BIGSERIAL PRIMARY KEY,
    place_id    BIGINT       NOT NULL REFERENCES parking_places(id) ON DELETE CASCADE,
    name        VARCHAR(50)  NOT NULL,
    level       VARCHAR(10),
    description VARCHAR(200)
);

-- Chennai: Zone A + B
INSERT INTO parking_zones (place_id, name, level, description)
SELECT id, 'Zone A', 'G', 'Ground Level - Terminal 1' FROM parking_places WHERE name LIKE 'Chennai%'
UNION ALL
SELECT id, 'Zone B', '1', 'First Floor - Terminal 2'  FROM parking_places WHERE name LIKE 'Chennai%';

-- Coimbatore: Zone A + B
INSERT INTO parking_zones (place_id, name, level, description)
SELECT id, 'Zone A', 'G', 'Ground Level'   FROM parking_places WHERE name LIKE 'Coimbatore%'
UNION ALL
SELECT id, 'Zone B', '1', 'First Floor'    FROM parking_places WHERE name LIKE 'Coimbatore%';

-- Madurai: Zone A + B
INSERT INTO parking_zones (place_id, name, level, description)
SELECT id, 'Zone A', 'G', 'Ground Level'   FROM parking_places WHERE name LIKE 'Madurai%'
UNION ALL
SELECT id, 'Zone B', '1', 'First Floor'    FROM parking_places WHERE name LIKE 'Madurai%';

-- Tiruchirappalli: Zone A + B
INSERT INTO parking_zones (place_id, name, level, description)
SELECT id, 'Zone A', 'G', 'Ground Level'   FROM parking_places WHERE name LIKE 'Tiruchirappalli%'
UNION ALL
SELECT id, 'Zone B', '1', 'First Floor'    FROM parking_places WHERE name LIKE 'Tiruchirappalli%';
