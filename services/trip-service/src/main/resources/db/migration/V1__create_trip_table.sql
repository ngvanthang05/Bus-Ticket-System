CREATE TABLE trips (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_id        UUID NOT NULL,
    vehicle_id      UUID NOT NULL,
    driver_id       UUID,
    assistant_id    UUID,
    departure_time  TIMESTAMP NOT NULL,
    arrival_time    TIMESTAMP NOT NULL,
    base_price      DOUBLE PRECISION NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT chk_trip_time CHECK (arrival_time > departure_time)
);

CREATE INDEX idx_trip_route_id ON trips(route_id);
CREATE INDEX idx_trip_vehicle_id ON trips(vehicle_id);
CREATE INDEX idx_trip_departure_time ON trips(departure_time);
CREATE INDEX idx_trip_status ON trips(status);