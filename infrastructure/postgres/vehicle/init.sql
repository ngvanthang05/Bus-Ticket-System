-- ============================================================
-- VEHICLE SERVICE DATABASE SCHEMA
-- Database: vehicle_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLE: vehicles (Xe)
-- ============================================================
CREATE TABLE IF NOT EXISTS vehicles (
                                        id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    license_plate   VARCHAR(20) NOT NULL UNIQUE,
    vehicle_name    VARCHAR(255) NOT NULL,
    vehicle_type    VARCHAR(50) NOT NULL,
    -- SEAT_BUS: Xe ghế ngồi, SLEEPER_BUS: Xe giường nằm, LIMOUSINE: Xe limousine
    total_seats     INTEGER NOT NULL,
    seat_rows       INTEGER NOT NULL,
    seat_cols       INTEGER NOT NULL,
    floors          INTEGER NOT NULL DEFAULT 1,
    manufacture_year INTEGER,
    brand           VARCHAR(100),
    model           VARCHAR(100),
    color           VARCHAR(50),
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE: Đang hoạt động, MAINTENANCE: Bảo trì, INACTIVE: Ngưng
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: seats (Ghế trên xe)
-- ============================================================
CREATE TABLE IF NOT EXISTS seats (
                                     id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vehicle_id      UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    seat_number     VARCHAR(10) NOT NULL,
    seat_row        INTEGER NOT NULL,
    seat_col        INTEGER NOT NULL,
    floor           INTEGER NOT NULL DEFAULT 1,
    seat_type       VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    -- NORMAL: Ghế thường, VIP: VIP, DOUBLE: Ghế đôi (limousine)
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(vehicle_id, seat_number)
    );

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_vehicles_status  ON vehicles(status);
CREATE INDEX IF NOT EXISTS idx_vehicles_type    ON vehicles(vehicle_type);
CREATE INDEX IF NOT EXISTS idx_seats_vehicle    ON seats(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_seats_active     ON seats(is_active);

-- ============================================================
-- FUNCTION: generate seats tự động khi thêm xe
-- ============================================================
CREATE OR REPLACE FUNCTION generate_seats_for_vehicle(
    p_vehicle_id UUID,
    p_rows INTEGER,
    p_cols INTEGER,
    p_floors INTEGER DEFAULT 1
)
RETURNS void AS $$
DECLARE
f INTEGER;
    r INTEGER;
    c INTEGER;
    seat_label VARCHAR(10);
    col_letter CHAR(1);
BEGIN
FOR f IN 1..p_floors LOOP
        FOR r IN 1..p_rows LOOP
            FOR c IN 1..p_cols LOOP
                col_letter := CHR(64 + c); -- A, B, C, D...
                IF p_floors > 1 THEN
                    seat_label := f || '-' || r || col_letter;
ELSE
                    seat_label := r || col_letter;
END IF;

INSERT INTO seats (vehicle_id, seat_number, seat_row, seat_col, floor)
VALUES (p_vehicle_id, seat_label, r, c, f)
    ON CONFLICT (vehicle_id, seat_number) DO NOTHING;
END LOOP;
END LOOP;
END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- SEED DATA
-- ============================================================
INSERT INTO vehicles (id, license_plate, vehicle_name, vehicle_type, total_seats, seat_rows, seat_cols, floors, brand) VALUES
                                                                                                                           ('v1000000-0000-0000-0000-000000000001', '51B-12345', 'Xe Ghế 45 Chỗ - 001', 'SEAT_BUS',    45, 11, 4, 1, 'Thaco'),
                                                                                                                           ('v2000000-0000-0000-0000-000000000002', '51B-23456', 'Xe Giường 40 Chỗ - 002', 'SLEEPER_BUS', 40, 10, 4, 2, 'Hyundai'),
                                                                                                                           ('v3000000-0000-0000-0000-000000000003', '51B-34567', 'Limousine 12 Chỗ - 003', 'LIMOUSINE',   12, 4,  3, 1, 'Ford')
    ON CONFLICT (license_plate) DO NOTHING;

-- Tạo ghế tự động
SELECT generate_seats_for_vehicle('v1000000-0000-0000-0000-000000000001', 11, 4, 1);
SELECT generate_seats_for_vehicle('v2000000-0000-0000-0000-000000000002', 10, 4, 2);
SELECT generate_seats_for_vehicle('v3000000-0000-0000-0000-000000000003', 4,  3, 1);

-- Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ language 'plpgsql';

CREATE OR REPLACE TRIGGER update_vehicles_updated_at
    BEFORE UPDATE ON vehicles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();