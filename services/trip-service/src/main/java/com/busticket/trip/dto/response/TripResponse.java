package com.xekhach.tripservice.dto.response;

import com.xekhach.tripservice.entity.TripStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {
    private UUID id;
    private UUID routeId;
    private UUID vehicleId;
    private UUID driverId;
    private UUID assistantId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double basePrice;
    private TripStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dữ liệu enrich (làm giàu) từ Route/Vehicle Service — optional, chỉ set khi gọi API "detail"
    private RouteResponse route;
    private VehicleResponse vehicle;
}