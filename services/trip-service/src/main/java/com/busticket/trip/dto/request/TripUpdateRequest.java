package com.xekhach.tripservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripUpdateRequest {
    private UUID driverId;
    private UUID assistantId;

    @Future(message = "departureTime phải ở tương lai")
    private LocalDateTime departureTime;

    @Future(message = "arrivalTime phải ở tương lai")
    private LocalDateTime arrivalTime;

    @Positive(message = "basePrice phải lớn hơn 0")
    private Double basePrice;
}