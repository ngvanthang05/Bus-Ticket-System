package com.xekhach.tripservice.repository;

import com.xekhach.tripservice.entity.Trip;
import com.xekhach.tripservice.entity.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    Page<Trip> findByRouteId(UUID routeId, Pageable pageable);

    Page<Trip> findByStatus(TripStatus status, Pageable pageable);

    Page<Trip> findByRouteIdAndDepartureTimeBetween(
            UUID routeId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Check trùng lịch: xe đã được gán cho 1 chuyến khác
     * có khung giờ giao nhau chưa (tránh 1 xe chạy 2 chuyến cùng lúc)
     */
    boolean existsByVehicleIdAndStatusNotInAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
            UUID vehicleId, java.util.List<TripStatus> excludedStatuses,
            LocalDateTime arrivalTime, LocalDateTime departureTime);
}