package com.urisik.backend.global.auth.oauth2;

import com.urisik.backend.global.auth.dto.CustomOAuth2User;
import com.urisik.backend.global.auth.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String refreshToken = jwtUtil.createRefreshToken(customUserDetails);

        addRefreshCookie(response, refreshToken);

        // 프론트 특정 url 로컬에서 : "http://localhost:5173/login/callback"
        response.sendRedirect("http://localhost:5173/login/callback");
    }
    // 배포 에선 "https://urisik.vercel.app/login/callback"

    private void addRefreshCookie(HttpServletResponse response, String value) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", value)
                .httpOnly(true)
                .secure(true)                 // SameSite=None이면 필수
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")            // ⭐ 핵심: 크로스 사이트 쿠키 전송 허용
                // .domain("api.urisik.com")  // 보통은 안 넣어도 됨 (필요할 때만)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
