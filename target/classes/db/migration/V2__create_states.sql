CREATE TABLE IF NOT EXISTS states (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(5)   NOT NULL UNIQUE
);

INSERT INTO states (name, code) VALUES
    ('Tamil Nadu',       'TN'),
    ('Maharashtra',      'MH'),
    ('Karnataka',        'KA'),
    ('Telangana',        'TS'),
    ('Delhi',            'DL'),
    ('Gujarat',          'GJ'),
    ('West Bengal',      'WB'),
    ('Rajasthan',        'RJ'),
    ('Uttar Pradesh',    'UP'),
    ('Kerala',           'KL');
