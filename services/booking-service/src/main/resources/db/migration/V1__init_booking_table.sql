CREATE TABLE bookings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_name   VARCHAR(255)        NOT NULL,
    phone           VARCHAR(20)         NOT NULL,
    trip_id         UUID                NOT NULL,
    seat_number     VARCHAR(10)         NOT NULL,
    status          VARCHAR(20)         NOT NULL,
    amount          NUMERIC(12,2),
    created_at      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT now(),
    version         BIGINT              NOT NULL DEFAULT 0
);

-- Chống double-booking ở tầng DB: 1 ghế trong 1 chuyến chỉ được có
-- tối đa 1 booking đang HOLD hoặc PAID (dùng partial unique index)
CREATE UNIQUE INDEX uq_trip_seat_active
    ON bookings (trip_id, seat_number)
    WHERE status IN ('HOLD', 'PAID');

CREATE INDEX idx_bookings_trip_id ON bookings (trip_id);
CREATE INDEX idx_bookings_status ON bookings (status);