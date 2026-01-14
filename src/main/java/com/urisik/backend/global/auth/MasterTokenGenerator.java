package com.urisik.backend.global.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class MasterTokenGenerator {

    public static void main(String[] args) {

        // ✅ application.yml 의 JWT_KEY 값과 동일해야 함(고정)
        String secret = "ZGh3YWlkc2F2ZXdhZXZ3b2EgMTM5ZXUgMDMxdWMyIHEyMiBAIDAgKTJFVio=";

        Long masterMemberId = 1L;       //  마스터 계정 id
        String role = "ROLE_ADMIN";     //  권한

        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Date now = new Date();
        Date expiry = Date.from(
                LocalDateTime.now()
                        .plusMonths(2)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        String token = Jwts.builder()
                .setSubject(String.valueOf(masterMemberId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("====== MASTER JWT (2 months) ======");
        System.out.println(token);
        System.out.println("expiresAt = " + expiry);
    }
}
