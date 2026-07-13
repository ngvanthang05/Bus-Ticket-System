package com.xekhach.vehicleservice.dto;

import com.xekhach.vehicleservice.entity.VehicleStatus;
import com.xekhach.vehicleservice.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {
    private UUID id;
    private String licensePlate;
    private VehicleType vehicleType;
    private Integer seatCount;
    private VehicleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}