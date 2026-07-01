-- ============================================================
-- ROUTE SERVICE DATABASE SCHEMA
-- Database: route_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLE: stops (Điểm đón/trả khách)
-- ============================================================
CREATE TABLE IF NOT EXISTS stops (
                                     id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(500),
    city        VARCHAR(100) NOT NULL,
    province    VARCHAR(100),
    latitude    DECIMAL(10, 8),
    longitude   DECIMAL(11, 8),
    stop_type   VARCHAR(50) NOT NULL DEFAULT 'STATION',
    -- STATION: Bến xe, TRANSFER: Trạm trung chuyển, ROADSIDE: Điểm dọc đường
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: routes (Tuyến xe)
-- ============================================================
CREATE TABLE IF NOT EXISTS routes (
                                      id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_code        VARCHAR(50) NOT NULL UNIQUE,
    name              VARCHAR(255) NOT NULL,
    departure_stop_id UUID NOT NULL REFERENCES stops(id),
    destination_stop_id UUID NOT NULL REFERENCES stops(id),
    distance_km       DECIMAL(8, 2) NOT NULL,
    duration_minutes  INTEGER NOT NULL,
    base_price        DECIMAL(12, 2) NOT NULL,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    description       TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: route_stops (Các điểm dừng trên tuyến)
-- ============================================================
CREATE TABLE IF NOT EXISTS route_stops (
                                           id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    route_id        UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    stop_id         UUID NOT NULL REFERENCES stops(id),
    stop_order      INTEGER NOT NULL,
    -- Thời gian dự kiến từ điểm xuất phát (phút)
    arrival_offset  INTEGER NOT NULL DEFAULT 0,
    departure_offset INTEGER NOT NULL DEFAULT 0,
    is_pickup       BOOLEAN NOT NULL DEFAULT TRUE,
    is_dropoff      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(route_id, stop_id),
    UNIQUE(route_id, stop_order)
    );

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_routes_departure    ON routes(departure_stop_id);
CREATE INDEX IF NOT EXISTS idx_routes_destination  ON routes(destination_stop_id);
CREATE INDEX IF NOT EXISTS idx_routes_active       ON routes(is_active);
CREATE INDEX IF NOT EXISTS idx_route_stops_route   ON route_stops(route_id);
CREATE INDEX IF NOT EXISTS idx_route_stops_stop    ON route_stops(stop_id);
CREATE INDEX IF NOT EXISTS idx_stops_city          ON stops(city);

-- ============================================================
-- SEED DATA
-- ============================================================
INSERT INTO stops (id, name, address, city, province, stop_type) VALUES
                                                                     ('s1000000-0000-0000-0000-000000000001', 'Bến xe Miền Đông', '292 Đinh Bộ Lĩnh, Bình Thạnh', 'Hồ Chí Minh', 'Hồ Chí Minh', 'STATION'),
                                                                     ('s2000000-0000-0000-0000-000000000002', 'Bến xe Miền Tây', '395 Kinh Dương Vương, Bình Tân', 'Hồ Chí Minh', 'Hồ Chí Minh', 'STATION'),
                                                                     ('s3000000-0000-0000-0000-000000000003', 'Bến xe Đà Lạt',   'Số 1 Tô Hiệu, Phường 3',         'Đà Lạt',     'Lâm Đồng',    'STATION'),
                                                                     ('s4000000-0000-0000-0000-000000000004', 'Bến xe Vũng Tàu', '52 Nam Kỳ Khởi Nghĩa',           'Vũng Tàu',   'Bà Rịa',      'STATION'),
                                                                     ('s5000000-0000-0000-0000-000000000005', 'Bến xe Cần Thơ',  '9 Hùng Vương, Ninh Kiều',        'Cần Thơ',    'Cần Thơ',     'STATION')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO routes (id, route_code, name, departure_stop_id, destination_stop_id, distance_km, duration_minutes, base_price) VALUES
                                                                                                                                 ('r1000000-0000-0000-0000-000000000001', 'HCM-DL',  'TP.HCM - Đà Lạt',  's1000000-0000-0000-0000-000000000001', 's3000000-0000-0000-0000-000000000003', 308, 360, 150000),
                                                                                                                                 ('r2000000-0000-0000-0000-000000000002', 'HCM-VT',  'TP.HCM - Vũng Tàu','s1000000-0000-0000-0000-000000000001', 's4000000-0000-0000-0000-000000000004', 125, 150, 80000),
                                                                                                                                 ('r3000000-0000-0000-0000-000000000003', 'HCM-CT',  'TP.HCM - Cần Thơ', 's2000000-0000-0000-0000-000000000002', 's5000000-0000-0000-0000-000000000005', 170, 180, 100000)
    ON CONFLICT (route_code) DO NOTHING;

-- ============================================================
-- TRIGGER: auto-update updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ language 'plpgsql';

CREATE OR REPLACE TRIGGER update_routes_updated_at
    BEFORE UPDATE ON routes FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_stops_updated_at
    BEFORE UPDATE ON stops FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();