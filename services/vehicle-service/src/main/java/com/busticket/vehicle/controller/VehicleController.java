package com.xekhach.vehicleservice.controller;

import com.xekhach.vehicleservice.common.ApiResponse;
import com.xekhach.vehicleservice.common.PageResponse;
import com.xekhach.vehicleservice.dto.VehicleRequest;
import com.xekhach.vehicleservice.dto.VehicleResponse;
import com.xekhach.vehicleservice.entity.VehicleStatus;
import com.xekhach.vehicleservice.entity.VehicleType;
import com.xekhach.vehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Vehicle", description = "Quản lý xe (phương tiện)")
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Tạo xe mới")
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> create(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo xe thành công", response));
    }

    @Operation(summary = "Cập nhật thông tin xe")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật xe thành công", response));
    }

    @Operation(summary = "Lấy thông tin xe theo id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable UUID id) {
        VehicleResponse response = vehicleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Xóa xe")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa xe thành công", null));
    }

    @Operation(summary = "Tìm kiếm / phân trang danh sách xe")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> search(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VehicleResponse> result = vehicleService.search(status, vehicleType, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }
}