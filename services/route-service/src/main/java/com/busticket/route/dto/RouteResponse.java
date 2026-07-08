package com.xekhach.routeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private String id;
    private String departure;
    private String destination;
    private Double distance;
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}