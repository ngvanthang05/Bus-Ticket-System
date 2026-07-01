package com.xekhach.auth.service.impl;

import com.xekhach.auth.dto.request.LoginRequest;
import com.xekhach.auth.dto.request.RegisterRequest;
import com.xekhach.auth.dto.response.AuthResponse;
import com.xekhach.auth.dto.response.UserResponse;
import com.xekhach.auth.entity.Role;
import com.xekhach.auth.entity.User;
import com.xekhach.auth.exception.DuplicateResourceException;
import com.xekhach.auth.exception.InvalidCredentialsException;
import com.xekhach.auth.mapper.UserMapper;
import com.xekhach.auth.repository.UserRepository;
import com.xekhach.auth.security.CustomUserDetails;
import com.xekhach.auth.security.JwtTokenProvider;
import com.xekhach.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email đã được sử dụng: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Số điện thoại đã được sử dụng: " + request.getPhone());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.KHACH_HANG)  // mặc định khi tự đăng ký luôn là khách hàng
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Đăng ký tài khoản thành công: email={}, id={}", saved.getEmail(), saved.getId());

        return userMapper.toUserResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));

            log.info("Đăng nhập thành công: email={}", request.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .expiresInMs(jwtTokenProvider.getAccessTokenExpirationMs())
                    .user(userMapper.toUserResponse(user))
                    .build();

        } catch (org.springframework.security.core.AuthenticationException ex) {
            log.warn("Đăng nhập thất bại cho email={}: {}", request.getEmail(), ex.getMessage());
            throw new InvalidCredentialsException("Email hoặc mật khẩu không đúng");
        }
    }
}