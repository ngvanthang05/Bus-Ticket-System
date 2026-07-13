package com.xekhach.tripservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    /**
     * Forward header Authorization từ request gốc (client -> Gateway -> Trip Service)
     * sang các request Feign gọi tới Route Service / Vehicle Service.
     * Vì Route/Vehicle Service cũng có filter xác thực JWT (giống Gateway),
     * nên request nội bộ vẫn cần mang theo token hợp lệ.
     */
    @Bean
    public RequestInterceptor authHeaderInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                }
            }
        };
    }
}