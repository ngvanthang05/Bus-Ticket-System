-- ============================================================
-- BOOKING SERVICE DATABASE SCHEMA
-- Database: booking_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- TABLE: customers (Snapshot thông tin khách khi đặt vé)
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
                                         id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID,           -- NULL nếu đặt qua quầy không login
    full_name   VARCHAR(255) NOT NULL,
    phone       VARCHAR(20) NOT NULL,
    email       VARCHAR(255),
    id_card     VARCHAR(20),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: bookings (Đơn đặt vé)
-- ============================================================
CREATE TABLE IF NOT EXISTS bookings (
                                        id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_code    VARCHAR(50) NOT NULL UNIQUE,
    customer_id     UUID NOT NULL REFERENCES customers(id),
    trip_id         UUID NOT NULL,
    -- Snapshot thông tin tuyến/chuyến khi đặt
    route_name      VARCHAR(255),
    departure_time  TIMESTAMP,
    pickup_stop_id  UUID,
    pickup_stop_name VARCHAR(255),
    dropoff_stop_id  UUID,
    dropoff_stop_name VARCHAR(255),
    --
    total_amount    DECIMAL(12, 2) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING: Chờ thanh toán, CONFIRMED: Đã xác nhận
    -- CANCELLED: Đã hủy, COMPLETED: Hoàn thành
    booked_by       UUID,  -- staff_id nếu bán tại quầy
    booking_channel VARCHAR(50) NOT NULL DEFAULT 'WEBSITE',
    -- WEBSITE, APP, COUNTER, AGENT
    notes           TEXT,
    cancelled_at    TIMESTAMP,
    cancel_reason   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: tickets (Vé xe - 1 booking có thể nhiều vé)
-- ============================================================
CREATE TABLE IF NOT EXISTS tickets (
                                       id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_code     VARCHAR(50) NOT NULL UNIQUE,
    booking_id      UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    trip_id         UUID NOT NULL,
    seat_id         UUID NOT NULL,
    seat_number     VARCHAR(10) NOT NULL,
    passenger_name  VARCHAR(255) NOT NULL,
    passenger_phone VARCHAR(20),
    price           DECIMAL(12, 2) NOT NULL,
    qr_code         TEXT NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'ISSUED',
    -- ISSUED: Đã phát hành, CHECKED_IN: Đã lên xe
    -- CANCELLED: Đã hủy, EXPIRED: Hết hạn
    checkin_at      TIMESTAMP,
    checkin_staff_id UUID,
    issued_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================================
-- TABLE: seat_locks (Giữ ghế tạm thời - backup cho Redis)
-- ============================================================
CREATE TABLE IF NOT EXISTS seat_locks (
                                          id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id     UUID NOT NULL,
    seat_id     UUID NOT NULL,
    booking_id  UUID,
    locked_by   UUID,
    locked_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMP NOT NULL,
    is_released BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(trip_id, seat_id)
    );

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_bookings_customer    ON bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_trip        ON bookings(trip_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status      ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_code        ON bookings(booking_code);
CREATE INDEX IF NOT EXISTS idx_tickets_booking      ON tickets(booking_id);
CREATE INDEX IF NOT EXISTS idx_tickets_trip         ON tickets(trip_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status       ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_code         ON tickets(ticket_code);
CREATE INDEX IF NOT EXISTS idx_seat_locks_trip_seat ON seat_locks(trip_id, seat_id);
CREATE INDEX IF NOT EXISTS idx_seat_locks_expires   ON seat_locks(expires_at);

-- ============================================================
-- FUNCTION: Generate booking code
-- ============================================================
CREATE OR REPLACE FUNCTION generate_booking_code()
RETURNS VARCHAR AS $$
BEGIN
RETURN 'BK' || TO_CHAR(NOW(), 'YYMMDD') || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 1, 6));
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- FUNCTION: Generate ticket code
-- ============================================================
CREATE OR REPLACE FUNCTION generate_ticket_code()
RETURNS VARCHAR AS $$
BEGIN
RETURN 'TK' || TO_CHAR(NOW(), 'YYMMDD') || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 1, 6));
END;
$$ LANGUAGE plpgsql;

-- Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ language 'plpgsql';

CREATE OR REPLACE TRIGGER update_bookings_updated_at
    BEFORE UPDATE ON bookings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE OR REPLACE TRIGGER update_tickets_updated_at
    BEFORE UPDATE ON tickets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE OR REPLACE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();