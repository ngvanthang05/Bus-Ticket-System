-- ============================================================
-- TRIP SERVICE DATABASE SCHEMA
-- Database: trip_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLE: trips (Chuyến xe)
-- ============================================================
CREATE TABLE IF NOT EXISTS trips (
                                     id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_code       VARCHAR(50) NOT NULL UNIQUE,
    route_id        UUID NOT NULL,
    vehicle_id      UUID NOT NULL,
    driver_id       UUID,
    co_driver_id    UUID,
    departure_time  TIMESTAMP NOT NULL,
    arrival_time    TIMESTAMP NOT NULL,
    base_price      DECIMAL(12, 2) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    -- SCHEDULED: Lên lịch, OPEN: Mở bán, FULL: Hết ghế
    -- DEPARTED: Đang chạy, ARRIVED: Đã đến, CANCELLED: Hủy
    total_seats     INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    notes           TEXT,
    created_by      UUID,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: trip_seats (Snapshot ghế theo từng chuyến)
-- Mỗi chuyến xe có danh sách ghế riêng,
-- tránh conflict khi 1 xe chạy nhiều chuyến trong ngày
-- ============================================================
CREATE TABLE IF NOT EXISTS trip_seats (
                                          id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id         UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    seat_id         UUID NOT NULL,         -- FK to vehicle_db.seats (cross-service reference)
    seat_number     VARCHAR(10) NOT NULL,
    seat_type       VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    floor           INTEGER NOT NULL DEFAULT 1,
    status          VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    -- AVAILABLE: Trống, LOCKED: Đang giữ (5 phút), BOOKED: Đã đặt
    price           DECIMAL(12, 2) NOT NULL,
    booking_id      UUID,
    locked_at       TIMESTAMP,
    locked_until    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(trip_id, seat_id)
    );

-- ============================================================
-- TABLE: trip_stops (Điểm dừng theo chuyến - có thể override từ route)
-- ============================================================
CREATE TABLE IF NOT EXISTS trip_stops (
                                          id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id         UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    stop_id         UUID NOT NULL,
    stop_name       VARCHAR(255) NOT NULL,
    stop_order      INTEGER NOT NULL,
    scheduled_time  TIMESTAMP,
    actual_time     TIMESTAMP,
    is_pickup       BOOLEAN NOT NULL DEFAULT TRUE,
    is_dropoff      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_trips_route_id       ON trips(route_id);
CREATE INDEX IF NOT EXISTS idx_trips_vehicle_id     ON trips(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_trips_departure      ON trips(departure_time);
CREATE INDEX IF NOT EXISTS idx_trips_status         ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trip_seats_trip      ON trip_seats(trip_id);
CREATE INDEX IF NOT EXISTS idx_trip_seats_status    ON trip_seats(status);
CREATE INDEX IF NOT EXISTS idx_trip_seats_booking   ON trip_seats(booking_id);

-- ============================================================
-- FUNCTION: Tự động release ghế bị lock quá 5 phút
-- ============================================================
CREATE OR REPLACE FUNCTION release_expired_seat_locks()
RETURNS void AS $$
BEGIN
UPDATE trip_seats
SET status = 'AVAILABLE',
    booking_id = NULL,
    locked_at = NULL,
    locked_until = NULL,
    updated_at = NOW()
WHERE status = 'LOCKED'
  AND locked_until < NOW();
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- TRIGGER: auto-update available_seats khi trip_seats thay đổi
-- ============================================================
CREATE OR REPLACE FUNCTION update_trip_available_seats()
RETURNS TRIGGER AS $$
BEGIN
UPDATE trips
SET available_seats = (
    SELECT COUNT(*) FROM trip_seats
    WHERE trip_id = COALESCE(NEW.trip_id, OLD.trip_id)
      AND status = 'AVAILABLE'
),
    updated_at = NOW()
WHERE id = COALESCE(NEW.trip_id, OLD.trip_id);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_update_available_seats
    AFTER INSERT OR UPDATE OR DELETE ON trip_seats
    FOR EACH ROW EXECUTE FUNCTION update_trip_available_seats();

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ language 'plpgsql';

CREATE OR REPLACE TRIGGER update_trips_updated_at
    BEFORE UPDATE ON trips FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_trip_seats_updated_at
    BEFORE UPDATE ON trip_seats FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();