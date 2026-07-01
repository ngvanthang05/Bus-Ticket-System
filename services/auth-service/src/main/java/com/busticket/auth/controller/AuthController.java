package com.xekhach.auth.controller;

import com.xekhach.auth.dto.request.LoginRequest;
import com.xekhach.auth.dto.request.RegisterRequest;
import com.xekhach.auth.dto.response.ApiResponse;
import com.xekhach.auth.dto.response.AuthResponse;
import com.xekhach.auth.dto.response.UserResponse;
import com.xekhach.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "API đăng ký / đăng nhập")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản khách hàng mới")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập, trả về JWT access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", auth));
    }
}