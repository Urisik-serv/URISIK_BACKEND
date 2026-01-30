package com.urisik.backend.global.auth.oauth2;

import com.urisik.backend.global.auth.dto.CustomOAuth2User;
import com.urisik.backend.global.auth.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();


        String token = jwtUtil.createRefreshToken(customUserDetails);

        response.addCookie(createRefreshCookie(token));
        //프론트 특정 url
        response.sendRedirect("https://urisik.vercel.app/login");
    }



    private Cookie createRefreshCookie(String value) {
        Cookie cookie = new Cookie("refresh_token", value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(14).getSeconds());
        // 운영에서는 HTTPS 필수
        cookie.setSecure(true);
        // SameSite는 Cookie API로 직접 설정이 어려워서 ResponseCookie를 추천(아래 참고)
        return cookie;
    }
}