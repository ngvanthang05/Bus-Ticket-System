package com.xekhach.tripservice.client;

import com.xekhach.tripservice.common.ApiResponse;
import com.xekhach.tripservice.dto.response.RouteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * "route-service" phải trùng với spring.application.name của Route Service
 * để Eureka/Consul (nếu có) hoặc cấu hình url tĩnh resolve đúng địa chỉ.
 * Ở đây dùng url tĩnh qua application.yml (không dùng service discovery)
 * vì hệ thống hiện tại chưa có Eureka Server.
 */
@FeignClient(
        name = "route-service",
        url = "${services.route-service.url}"
)
public interface RouteClient {

    @GetMapping("/api/v1/routes/{id}")
    ApiResponse<RouteResponse> getRouteById(@PathVariable("id") UUID id);
}