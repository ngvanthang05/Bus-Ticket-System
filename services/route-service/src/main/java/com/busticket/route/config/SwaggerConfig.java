package com.xekhach.routeservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI routeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Route Service API")
                        .description("Quản lý tuyến xe - Hệ thống Xe Khách")
                        .version("v1.0.0"));
    }
}