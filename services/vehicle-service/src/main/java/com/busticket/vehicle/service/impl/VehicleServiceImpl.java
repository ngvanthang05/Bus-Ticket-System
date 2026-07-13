package com.xekhach.vehicleservice.service.impl;

import com.xekhach.vehicleservice.dto.VehicleRequest;
import com.xekhach.vehicleservice.dto.VehicleResponse;
import com.xekhach.vehicleservice.entity.Vehicle;
import com.xekhach.vehicleservice.entity.VehicleStatus;
import com.xekhach.vehicleservice.entity.VehicleType;
import com.xekhach.vehicleservice.exception.DuplicateResourceException;
import com.xekhach.vehicleservice.exception.ResourceNotFoundException;
import com.xekhach.vehicleservice.mapper.VehicleMapper;
import com.xekhach.vehicleservice.repository.VehicleRepository;
import com.xekhach.vehicleservice.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        log.info("Creating vehicle with license plate: {}", request.getLicensePlate());

        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new DuplicateResourceException(
                    "Biển số xe '" + request.getLicensePlate() + "' đã tồn tại trong hệ thống");
        }

        Vehicle vehicle = vehicleMapper.toEntity(request);
        if (vehicle.getStatus() == null) {
            vehicle.setStatus(VehicleStatus.ACTIVE);
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with id: {}", saved.getId());
        return vehicleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public VehicleResponse update(UUID id, VehicleRequest request) {
        log.info("Updating vehicle id: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với id: " + id));

        if (request.getLicensePlate() != null &&
                vehicleRepository.existsByLicensePlateAndIdNot(request.getLicensePlate(), id)) {
            throw new DuplicateResourceException(
                    "Biển số xe '" + request.getLicensePlate() + "' đã được xe khác sử dụng");
        }

        vehicleMapper.updateEntityFromRequest(request, vehicle);
        Vehicle updated = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully: {}", updated.getId());
        return vehicleMapper.toResponse(updated);
    }

    @Override
    public VehicleResponse getById(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với id: " + id));
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting vehicle id: {}", id);
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy xe với id: " + id);
        }
        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted: {}", id);
    }

    @Override
    public Page<VehicleResponse> search(
            VehicleStatus status, VehicleType vehicleType, String keyword, Pageable pageable) {
        return vehicleRepository.search(status, vehicleType, keyword, pageable)
                .map(vehicleMapper::toResponse);
    }
}