package com.xekhach.routeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotBlank(message = "Điểm đi không được để trống")
    private String departure;

    @NotBlank(message = "Điểm đến không được để trống")
    private String destination;

    @NotNull(message = "Khoảng cách không được để trống")
    @Positive(message = "Khoảng cách phải lớn hơn 0")
    private Double distance;

    @NotNull(message = "Thời gian dự kiến không được để trống")
    @Positive(message = "Thời gian dự kiến phải lớn hơn 0")
    private Integer duration;
}