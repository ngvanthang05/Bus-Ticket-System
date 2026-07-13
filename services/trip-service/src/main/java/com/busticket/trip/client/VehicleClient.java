package com.xekhach.tripservice.client;

import com.xekhach.tripservice.common.ApiResponse;
import com.xekhach.tripservice.dto.response.VehicleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "vehicle-service",
        url = "${services.vehicle-service.url}"
)
public interface VehicleClient {

    @GetMapping("/api/v1/vehicles/{id}")
    ApiResponse<VehicleResponse> getVehicleById(@PathVariable("id") UUID id);
}