package com.urisik.backend.global.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // ✅ 헤더 없으면 그냥 다음으로 (permitAll API들도 있으니까)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());

        try {
            // ✅ 유효 + access 토큰인지 확인까지 하고 싶으면 isAccess() 추가 추천
            if (!jwtUtil.isValid(token)) {
                throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
            }

            Long memberId = jwtUtil.getMemberId(token);
            String role = jwtUtil.getRole(token);

            if (role == null) {
                throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
            }

            var auth = new UsernamePasswordAuthenticationToken(
                    memberId,
                    null,
                    List.of(new SimpleGrantedAuthority(role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID); // 또는 TOKEN_EXPIRED로 분리 가능
        } catch (JwtException e) {
            throw new AuthenExcetion(AuthErrorCode.TOKEN_NOT_VALID);
        }
    }
}