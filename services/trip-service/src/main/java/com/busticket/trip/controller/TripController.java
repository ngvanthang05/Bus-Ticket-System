package com.xekhach.tripservice.controller;

import com.xekhach.tripservice.common.ApiResponse;
import com.xekhach.tripservice.common.PageResponse;
import com.xekhach.tripservice.dto.request.TripCreateRequest;
import com.xekhach.tripservice.dto.request.TripStatusUpdateRequest;
import com.xekhach.tripservice.dto.request.TripUpdateRequest;
import com.xekhach.tripservice.dto.response.TripResponse;
import com.xekhach.tripservice.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trip Management", description = "Quản lý chuyến xe")
public class TripController {

    private final TripService tripService;

    @PostMapping
    @Operation(summary = "Tạo chuyến xe mới")
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(
            @Valid @RequestBody TripCreateRequest request) {
        TripResponse response = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo chuyến xe thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết chuyến xe (enrich=true để lấy kèm thông tin route/vehicle)")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean enrich) {
        TripResponse response = tripService.getTripById(id, enrich);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin chuyến xe thành công"));
    }

    @GetMapping
    @Operation(summary = "Tìm kiếm / phân trang danh sách chuyến xe")
    public ResponseEntity<ApiResponse<PageResponse<TripResponse>>> searchTrips(
            @RequestParam(required = false) UUID routeId,
            Pageable pageable) {
        var page = tripService.searchTrips(routeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page), "Tìm kiếm thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin chuyến xe")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @PathVariable UUID id,
            @Valid @RequestBody TripUpdateRequest request) {
        TripResponse response = tripService.updateTrip(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật chuyến xe thành công"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái chuyến xe")
    public ResponseEntity<ApiResponse<TripResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TripStatusUpdateRequest request) {
        TripResponse response = tripService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái thành công"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa chuyến xe (chỉ khi ở trạng thái SCHEDULED)")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable UUID id) {
        tripService.deleteTrip(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa chuyến xe thành công"));
    }
}