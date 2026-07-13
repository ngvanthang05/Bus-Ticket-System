package com.xekhach.vehicleservice.dto;

import com.xekhach.vehicleservice.entity.VehicleStatus;
import com.xekhach.vehicleservice.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequest {

    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(
        regexp = "^[0-9]{2}[A-Z]{1,2}[0-9]?-[0-9]{3,5}(\\.[0-9]{2})?$",
        message = "Biển số xe không đúng định dạng (VD: 51G-12345)"
    )
    private String licensePlate;

    @NotNull(message = "Loại xe không được để trống")
    private VehicleType vehicleType;

    @NotNull(message = "Số chỗ ngồi không được để trống")
    @Min(value = 1, message = "Số chỗ ngồi phải lớn hơn 0")
    private Integer seatCount;

    // Optional khi update; khi create sẽ mặc định ACTIVE nếu null
    private VehicleStatus status;
}