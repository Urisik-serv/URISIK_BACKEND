package com.urisik.backend.global.auth.jwt;

import com.urisik.backend.global.auth.dto.CustomOAuth2User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessExpiration;
    private final Duration refreshExpiration;

    public JwtUtil(
            @Value("${jwt.token.secretKey}") String secret,
            @Value("${jwt.token.expiration.access}") Long accessExpiration,
            @Value("${jwt.token.expiration.refresh}") Long refreshExp
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofMillis(accessExpiration);
        this.refreshExpiration = Duration.ofMillis(refreshExp);
    }

    /** Access Token 생성 */
    public String createAccessToken(Long memberId,String role) {
        Instant now = Instant.now();


        return Jwts.builder()
                .subject(String.valueOf(memberId))      // PK 들어감
                .claim("role", role)
                .claim("typ", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(CustomOAuth2User member) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(member.getId()))
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshExpiration)))
                .signWith(secretKey)
                .compact();
    }


    /** 토큰 유효성 검증 */
    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }


    // refresh 인지
    public boolean isRefresh(String token) {
        Claims claims = getClaims(token).getPayload();
        return "refresh".equals(claims.get("typ", String.class));
    }



    /** Claims 파싱 */
    private Jws<Claims> getClaims(String token) throws JwtException {

        return Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token);
    }

    public Long getMemberId(String token) {
        Claims claims = getClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }

    public String getRole(String token) {
        Claims claims = getClaims(token).getPayload();
        return claims.get("role", String.class);
    }



}
