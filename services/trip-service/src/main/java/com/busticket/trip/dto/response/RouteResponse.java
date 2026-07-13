package com.xekhach.tripservice.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse {
    private UUID id;
    private String departure;
    private String destination;
    private Double distance;
    private Integer duration;
}