package com.xekhach.routeservice.controller;

import com.xekhach.routeservice.dto.ApiResponse;
import com.xekhach.routeservice.dto.PageResponse;
import com.xekhach.routeservice.dto.RouteRequest;
import com.xekhach.routeservice.dto.RouteResponse;
import com.xekhach.routeservice.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Tag(name = "Route API", description = "Quản lý tuyến xe")
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "Tạo tuyến xe mới")
    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> create(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = routeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tuyến xe thành công", response));
    }

    @Operation(summary = "Lấy thông tin tuyến xe theo id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> getById(@PathVariable String id) {
        RouteResponse response = routeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công", response));
    }

    @Operation(summary = "Tìm kiếm / danh sách tuyến xe có phân trang")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RouteResponse>>> search(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        PageResponse<RouteResponse> response =
                routeService.search(departure, destination, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", response));
    }

    @Operation(summary = "Cập nhật tuyến xe")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> update(
            @PathVariable String id, @Valid @RequestBody RouteRequest request) {
        RouteResponse response = routeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", response));
    }

    @Operation(summary = "Xóa tuyến xe")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        routeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thành công", null));
    }
}