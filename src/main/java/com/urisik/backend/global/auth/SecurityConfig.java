package com.urisik.backend.global.auth;


import com.urisik.backend.global.auth.jwt.JwtAuthFilter;
import com.urisik.backend.global.auth.jwt.JwtUtil;
import com.urisik.backend.global.auth.oauth2.CustomSuccessHandler;
import com.urisik.backend.global.auth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig  {

    //소셜 로그인 회원 인증 만들기 절차
    private final CustomOAuth2UserService customOAuth2UserService;
    //JWT 토큰 생성 검증 절차
    private final JwtUtil jwtUtil;
    //JWT 토큰 쿠키에 담기
    private final CustomSuccessHandler customSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {}) // 아래 corsConfigurationSource()랑 연결

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                ) // 🔥 JWT 인증에서는 세션을 절대 사용하지 않게 설정// 🛑 HTML 폼 로그인 / 기본 로그아웃 비활성화

                .formLogin(form -> form.disable())

                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)


                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        // 1. ✅ 완전 공개 (회원가입/로그인, 문서, 정적 리소스 등)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/**",
                                "/auth/signup",
                                "/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        /*

                        // 2. ✅ 비로그인도 볼 수 있지만, 로그인하면 더 많은 정보 보여줄 수도 있는 GET API들
                        //    (필요하면 여기 추가)
                        .requestMatchers(
                                ""
                        ).permitAll()

                        // 3. 🔒 로그인 필수 기능들
                        .requestMatchers(
                                ""
                        ).authenticated()

                        // 4. 나머지 다 막기 (안 쓰는 이상한 URL 접근 방지용)
                        .anyRequest().authenticated()

                         */
                );

        return http.build();
    }


    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil);
    }


    /*
    // 🌐 CORS 설정 (프론트 도메인 넣기)
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var config = new org.springframework.web.cors.CorsConfiguration();

        config.setAllowedOrigins(
                List.of("http://localhost:5173")
        );
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

     */
}




