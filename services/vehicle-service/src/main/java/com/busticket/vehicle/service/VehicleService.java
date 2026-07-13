package com.xekhach.vehicleservice.service;

import com.xekhach.vehicleservice.dto.VehicleRequest;
import com.xekhach.vehicleservice.dto.VehicleResponse;
import com.xekhach.vehicleservice.entity.VehicleStatus;
import com.xekhach.vehicleservice.entity.VehicleType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {
    VehicleResponse create(VehicleRequest request);
    VehicleResponse update(UUID id, VehicleRequest request);
    VehicleResponse getById(UUID id);
    void delete(UUID id);
    org.springframework.data.domain.Page<VehicleResponse> search(
            VehicleStatus status, VehicleType vehicleType, String keyword, Pageable pageable);
}