-- Performance indexes
CREATE INDEX IF NOT EXISTS idx_users_email         ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role          ON users(role);
CREATE INDEX IF NOT EXISTS idx_places_active       ON parking_places(active);
CREATE INDEX IF NOT EXISTS idx_places_state        ON parking_places(state_id);
CREATE INDEX IF NOT EXISTS idx_zones_place         ON parking_zones(place_id);
CREATE INDEX IF NOT EXISTS idx_spaces_zone         ON parking_spaces(zone_id);
CREATE INDEX IF NOT EXISTS idx_spaces_status       ON parking_spaces(status);
CREATE INDEX IF NOT EXISTS idx_vehicles_plate      ON vehicles(plate_number);
CREATE INDEX IF NOT EXISTS idx_reservations_ref    ON reservations(reference);
CREATE INDEX IF NOT EXISTS idx_reservations_space  ON reservations(space_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_sessions_vehicle    ON vehicle_sessions(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_sessions_place      ON vehicle_sessions(place_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status     ON vehicle_sessions(status);
CREATE INDEX IF NOT EXISTS idx_place_staff_user    ON place_staff(user_id);
CREATE INDEX IF NOT EXISTS idx_place_staff_place   ON place_staff(place_id);
CREATE INDEX IF NOT EXISTS idx_payments_session    ON payments(session_id);
