package com.xekhach.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        // jwtSecret phải là chuỗi base64 >= 256 bit cho thuật toán HS256
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);

        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT hết hạn: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT không được hỗ trợ: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT sai định dạng: {}", ex.getMessage());
        } catch (SignatureException ex) {
            log.warn("Chữ ký JWT không hợp lệ: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT rỗng hoặc null: {}", ex.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}