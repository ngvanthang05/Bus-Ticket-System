package com.xekhach.tripservice.service.impl;

import com.xekhach.tripservice.client.RouteClient;
import com.xekhach.tripservice.client.VehicleClient;
import com.xekhach.tripservice.common.ApiResponse;
import com.xekhach.tripservice.dto.request.TripCreateRequest;
import com.xekhach.tripservice.dto.request.TripStatusUpdateRequest;
import com.xekhach.tripservice.dto.request.TripUpdateRequest;
import com.xekhach.tripservice.dto.response.RouteResponse;
import com.xekhach.tripservice.dto.response.TripResponse;
import com.xekhach.tripservice.dto.response.VehicleResponse;
import com.xekhach.tripservice.entity.Trip;
import com.xekhach.tripservice.entity.TripStatus;
import com.xekhach.tripservice.exception.ExternalServiceException;
import com.xekhach.tripservice.exception.InvalidTripStateException;
import com.xekhach.tripservice.exception.ResourceNotFoundException;
import com.xekhach.tripservice.mapper.TripMapper;
import com.xekhach.tripservice.repository.TripRepository;
import com.xekhach.tripservice.service.TripService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final RouteClient routeClient;
    private final VehicleClient vehicleClient;

    private static final List<TripStatus> ACTIVE_STATUSES =
            List.of(TripStatus.SCHEDULED, TripStatus.OPEN_FOR_SALE, TripStatus.RUNNING);

    @Override
    @Transactional
    public TripResponse createTrip(TripCreateRequest request) {
        log.info("Creating trip for routeId={}, vehicleId={}", request.getRouteId(), request.getVehicleId());

        // 1. Validate route tồn tại (gọi Route Service qua Feign)
        RouteResponse route = fetchRoute(request.getRouteId());

        // 2. Validate vehicle tồn tại và đang ACTIVE (gọi Vehicle Service qua Feign)
        VehicleResponse vehicle = fetchVehicle(request.getVehicleId());
        if (!"ACTIVE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new InvalidTripStateException(
                    "Xe " + vehicle.getLicensePlate() + " không ở trạng thái ACTIVE, không thể gán chuyến");
        }

        // 3. Validate thời gian
        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new InvalidTripStateException("arrivalTime phải sau departureTime");
        }

        // 4. Validate xe không bị trùng lịch chạy
        boolean conflict = tripRepository
                .existsByVehicleIdAndStatusNotInAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
                        request.getVehicleId(),
                        List.of(TripStatus.CANCELLED, TripStatus.COMPLETED),
                        request.getArrivalTime(),
                        request.getDepartureTime());
        if (conflict) {
            throw new InvalidTripStateException(
                    "Xe " + vehicle.getLicensePlate() + " đã được gán cho 1 chuyến khác trùng khung giờ");
        }

        Trip trip = tripMapper.toEntity(request);
        trip.setStatus(TripStatus.SCHEDULED);
        Trip saved = tripRepository.save(trip);

        log.info("Trip created successfully, id={}", saved.getId());

        TripResponse response = tripMapper.toResponse(saved);
        response.setRoute(route);
        response.setVehicle(vehicle);
        return response;
    }

    @Override
    public TripResponse getTripById(UUID id, boolean enrich) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe id=" + id));

        TripResponse response = tripMapper.toResponse(trip);
        if (enrich) {
            response.setRoute(fetchRoute(trip.getRouteId()));
            response.setVehicle(fetchVehicle(trip.getVehicleId()));
        }
        return response;
    }

    @Override
    public Page<TripResponse> searchTrips(UUID routeId, Pageable pageable) {
        Page<Trip> page = (routeId != null)
                ? tripRepository.findByRouteId(routeId, pageable)
                : tripRepository.findAll(pageable);
        return page.map(tripMapper::toResponse);
    }

    @Override
    @Transactional
    public TripResponse updateTrip(UUID id, TripUpdateRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe id=" + id));

        if (trip.getStatus() == TripStatus.RUNNING || trip.getStatus() == TripStatus.COMPLETED) {
            throw new InvalidTripStateException("Không thể sửa chuyến đang chạy hoặc đã hoàn thành");
        }

        if (request.getDriverId() != null) trip.setDriverId(request.getDriverId());
        if (request.getAssistantId() != null) trip.setAssistantId(request.getAssistantId());
        if (request.getDepartureTime() != null) trip.setDepartureTime(request.getDepartureTime());
        if (request.getArrivalTime() != null) trip.setArrivalTime(request.getArrivalTime());
        if (request.getBasePrice() != null) trip.setBasePrice(request.getBasePrice());

        if (trip.getArrivalTime().isBefore(trip.getDepartureTime())
                || trip.getArrivalTime().isEqual(trip.getDepartureTime())) {
            throw new InvalidTripStateException("arrivalTime phải sau departureTime");
        }

        Trip updated = tripRepository.save(trip);
        log.info("Trip updated, id={}", id);
        return tripMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public TripResponse updateStatus(UUID id, TripStatusUpdateRequest request) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe id=" + id));

        validateStatusTransition(trip.getStatus(), request.getStatus());
        trip.setStatus(request.getStatus());
        Trip updated = tripRepository.save(trip);

        log.info("Trip {} status changed to {}", id, request.getStatus());
        // TODO (bước sau): publish Kafka event "trip.status.changed" để Booking/Notification Service lắng nghe
        return tripMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTrip(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe id=" + id));

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new InvalidTripStateException("Chỉ có thể xóa chuyến ở trạng thái SCHEDULED");
        }
        tripRepository.delete(trip);
        log.info("Trip deleted, id={}", id);
    }

    // ================== Private helpers ==================

    private RouteResponse fetchRoute(UUID routeId) {
        try {
            ApiResponse<RouteResponse> res = routeClient.getRouteById(routeId);
            if (res == null || res.getData() == null) {
                throw new ResourceNotFoundException("Không tìm thấy tuyến xe id=" + routeId);
            }
            return res.getData();
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Không tìm thấy tuyến xe id=" + routeId);
        } catch (FeignException ex) {
            log.error("Gọi Route Service thất bại: {}", ex.getMessage());
            throw new ExternalServiceException("Không thể kết nối Route Service, vui lòng thử lại sau", ex);
        }
    }

    private VehicleResponse fetchVehicle(UUID vehicleId) {
        try {
            ApiResponse<VehicleResponse> res = vehicleClient.getVehicleById(vehicleId);
            if (res == null || res.getData() == null) {
                throw new ResourceNotFoundException("Không tìm thấy xe id=" + vehicleId);
            }
            return res.getData();
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Không tìm thấy xe id=" + vehicleId);
        } catch (FeignException ex) {
            log.error("Gọi Vehicle Service thất bại: {}", ex.getMessage());
            throw new ExternalServiceException("Không thể kết nối Vehicle Service, vui lòng thử lại sau", ex);
        }
    }

    private void validateStatusTransition(TripStatus current, TripStatus next) {
        boolean valid = switch (current) {
            case SCHEDULED -> next == TripStatus.OPEN_FOR_SALE || next == TripStatus.CANCELLED;
            case OPEN_FOR_SALE -> next == TripStatus.CLOSED || next == TripStatus.CANCELLED;
            case CLOSED -> next == TripStatus.RUNNING || next == TripStatus.CANCELLED;
            case RUNNING -> next == TripStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!valid) {
            throw new InvalidTripStateException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + next);
        }
    }
}