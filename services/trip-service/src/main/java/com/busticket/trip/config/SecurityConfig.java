package com.xekhach.tripservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    // Cùng JWT secret với toàn hệ thống (Gateway, Auth Service...) — theo đúng learning đã ghi nhớ
    private final String jwtSecret;

    public SecurityConfig(org.springframework.core.env.Environment env) {
        this.jwtSecret = env.getProperty("jwt.secret");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .requestMatchers("GET", "/api/v1/trips/**").hasAnyRole(
                                "ADMIN", "DIEU_HANH", "NHAN_VIEN_BAN_VE", "TAI_XE", "KHACH_HANG")
                        .requestMatchers("POST", "/api/v1/trips/**").hasAnyRole("ADMIN", "DIEU_HANH")
                        .requestMatchers("PUT", "/api/v1/trips/**").hasAnyRole("ADMIN", "DIEU_HANH")
                        .requestMatchers("PATCH", "/api/v1/trips/**").hasAnyRole("ADMIN", "DIEU_HANH")
                        .requestMatchers("DELETE", "/api/v1/trips/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));
        return http.build();
    }

    private NimbusJwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}