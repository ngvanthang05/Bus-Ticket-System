package com.xekhach.tripservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripCreateRequest {

    @NotNull(message = "routeId không được để trống")
    private UUID routeId;

    @NotNull(message = "vehicleId không được để trống")
    private UUID vehicleId;

    private UUID driverId;

    private UUID assistantId;

    @NotNull(message = "departureTime không được để trống")
    @Future(message = "departureTime phải ở tương lai")
    private LocalDateTime departureTime;

    @NotNull(message = "arrivalTime không được để trống")
    @Future(message = "arrivalTime phải ở tương lai")
    private LocalDateTime arrivalTime;

    @NotNull(message = "basePrice không được để trống")
    @Positive(message = "basePrice phải lớn hơn 0")
    private Double basePrice;
}