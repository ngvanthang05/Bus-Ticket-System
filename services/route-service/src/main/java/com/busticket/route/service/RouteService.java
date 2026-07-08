package com.xekhach.routeservice.service;

import com.xekhach.routeservice.dto.PageResponse;
import com.xekhach.routeservice.dto.RouteRequest;
import com.xekhach.routeservice.dto.RouteResponse;

public interface RouteService {
    RouteResponse create(RouteRequest request);
    RouteResponse getById(String id);
    PageResponse<RouteResponse> search(String departure, String destination, int page, int size, String sortBy, String direction);
    RouteResponse update(String id, RouteRequest request);
    void delete(String id);
}