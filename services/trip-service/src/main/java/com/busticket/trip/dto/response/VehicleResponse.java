package com.xekhach.tripservice.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {
    private UUID id;
    private String licensePlate;
    private String vehicleType;
    private Integer seatCount;
    private String status; // ACTIVE, MAINTENANCE, INACTIVE
}