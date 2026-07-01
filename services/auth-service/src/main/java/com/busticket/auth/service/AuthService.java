package com.xekhach.auth.service;

import com.xekhach.auth.dto.request.LoginRequest;
import com.xekhach.auth.dto.request.RegisterRequest;
import com.xekhach.auth.dto.response.AuthResponse;
import com.xekhach.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}