package com.xekhach.auth.controller;

import com.xekhach.auth.dto.response.ApiResponse;
import com.xekhach.auth.dto.response.UserResponse;
import com.xekhach.auth.entity.User;
import com.xekhach.auth.exception.ResourceNotFoundException;
import com.xekhach.auth.mapper.UserMapper;
import com.xekhach.auth.repository.UserRepository;
import com.xekhach.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint minh hoạ cho protected API + pagination + role-based access.
 * /me dùng cho mọi user đã login.
 * Danh sách user (GET /) chỉ ADMIN mới được gọi (đã khai báo ở SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "API quản lý user - cần JWT")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin tài khoản đang đăng nhập")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        return ApiResponse.success(userMapper.toUserResponse(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Danh sách user (phân trang) - chỉ Admin")
    public ApiResponse<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable).map(userMapper::toUserResponse);
        return ApiResponse.success(page);
    }
}