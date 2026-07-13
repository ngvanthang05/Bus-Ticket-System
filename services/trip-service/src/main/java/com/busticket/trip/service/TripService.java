package com.xekhach.tripservice.service;

import com.xekhach.tripservice.dto.request.TripCreateRequest;
import com.xekhach.tripservice.dto.request.TripStatusUpdateRequest;
import com.xekhach.tripservice.dto.request.TripUpdateRequest;
import com.xekhach.tripservice.dto.response.TripResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TripService {
    TripResponse createTrip(TripCreateRequest request);
    TripResponse getTripById(UUID id, boolean enrich);
    org.springframework.data.domain.Page<TripResponse> searchTrips(UUID routeId, Pageable pageable);
    TripResponse updateTrip(UUID id, TripUpdateRequest request);
    TripResponse updateStatus(UUID id, TripStatusUpdateRequest request);
    void deleteTrip(UUID id);
}