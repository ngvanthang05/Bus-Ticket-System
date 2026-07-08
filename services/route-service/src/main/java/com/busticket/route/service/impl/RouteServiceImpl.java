package com.xekhach.routeservice.service.impl;

import com.xekhach.routeservice.dto.PageResponse;
import com.xekhach.routeservice.dto.RouteRequest;
import com.xekhach.routeservice.dto.RouteResponse;
import com.xekhach.routeservice.entity.Route;
import com.xekhach.routeservice.exception.ResourceNotFoundException;
import com.xekhach.routeservice.repository.RouteRepository;
import com.xekhach.routeservice.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public RouteResponse create(RouteRequest request) {
        if (routeRepository.existsByDepartureIgnoreCaseAndDestinationIgnoreCase(
                request.getDeparture(), request.getDestination())) {
            log.warn("Route already exists: {} -> {}", request.getDeparture(), request.getDestination());
            throw new IllegalArgumentException("Tuyến xe từ '" + request.getDeparture() +
                    "' đến '" + request.getDestination() + "' đã tồn tại");
        }

        Route route = Route.builder()
                .departure(request.getDeparture())
                .destination(request.getDestination())
                .distance(request.getDistance())
                .duration(request.getDuration())
                .build();

        Route saved = routeRepository.save(route);
        log.info("Created route id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getById(String id) {
        Route route = findRouteOrThrow(id);
        return toResponse(route);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RouteResponse> search(String departure, String destination,
                                               int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String dep = departure == null ? "" : departure;
        String dest = destination == null ? "" : destination;

        Page<Route> routePage = routeRepository
                .findByDepartureContainingIgnoreCaseAndDestinationContainingIgnoreCase(dep, dest, pageable);

        Page<RouteResponse> responsePage = routePage.map(this::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public RouteResponse update(String id, RouteRequest request) {
        Route route = findRouteOrThrow(id);
        route.setDeparture(request.getDeparture());
        route.setDestination(request.getDestination());
        route.setDistance(request.getDistance());
        route.setDuration(request.getDuration());

        Route updated = routeRepository.save(route);
        log.info("Updated route id={}", id);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Route route = findRouteOrThrow(id);
        routeRepository.delete(route);
        log.info("Deleted route id={}", id);
    }

    private Route findRouteOrThrow(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến xe với id: " + id));
    }

    private RouteResponse toResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .departure(route.getDeparture())
                .destination(route.getDestination())
                .distance(route.getDistance())
                .duration(route.getDuration())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }
}