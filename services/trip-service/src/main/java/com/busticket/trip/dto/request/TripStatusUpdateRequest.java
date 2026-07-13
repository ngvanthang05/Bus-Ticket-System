package com.xekhach.tripservice.dto.request;

import com.xekhach.tripservice.entity.TripStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripStatusUpdateRequest {

    @NotNull(message = "status không được để trống")
    private TripStatus status;
}