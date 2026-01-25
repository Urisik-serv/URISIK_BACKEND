package com.urisik.backend.global.auth.controller;

import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.MemberRepository;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.code.GeneralSuccessCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import com.urisik.backend.global.auth.dto.AccessTokenDto;
import com.urisik.backend.global.auth.dto.res.LogoutResponse;
import com.urisik.backend.global.auth.exception.AuthenExcetion;
import com.urisik.backend.global.auth.exception.code.AuthErrorCode;
import com.urisik.backend.global.auth.exception.code.AuthSuccessCode;
import com.urisik.backend.global.auth.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @PostMapping("/reissue")
    public ApiResponse<AccessTokenDto> reissue(
            @CookieValue(value = "refresh_token" , required = false) String refreshToken) {



        // 비어있거나, 유효하거나 , 리프레시 토큰(어쎄스로 속일수도) 이어야함
        if (refreshToken == null) {
            throw new AuthenExcetion(AuthErrorCode.No_Token);
        }
        if(!jwtUtil.isValid(refreshToken)){
            throw new AuthenExcetion(AuthErrorCode.Token_Not_Vaild);
        }
        if(!jwtUtil.isRefresh(refreshToken)){
            throw new AuthenExcetion(AuthErrorCode.Not_Refresh_Token);

        }

        Long memberId = jwtUtil.getMemberId(refreshToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenExcetion(AuthErrorCode.No_Member));

        String accessToken = jwtUtil.createAccessToken(memberId, member.getRole());

        return ApiResponse.onSuccess(AuthSuccessCode.Login_Access_Token, new AccessTokenDto(accessToken));
    }
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // 운영 HTTPS면 true, 로컬 http 테스트면 false로 해야 브라우저가 안 막음
        cookie.setMaxAge(0);    // ✅ 삭제
        response.addCookie(cookie);

        SecurityContextHolder.clearContext(); // 선택

        return ApiResponse.onSuccess(
                AuthSuccessCode.Logout_Suc,
                LogoutResponse.builder().logoutSuccess(true).deleteSuccess(false).build()
        );
    }


    @PostMapping("/delete")
    public ApiResponse<LogoutResponse> withdraw(HttpServletResponse response) {

        // ✅ JwtAuthFilter에서 principal = memberId 넣어둔 상태라고 가정
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal() == null) {
            throw new AuthenExcetion(AuthErrorCode.No_Token);
        }

        Long memberId;
        try {
            memberId = (Long) auth.getPrincipal();
        } catch (ClassCastException e) {
            throw new GeneralException(GeneralErrorCode.UNAUTHORIZED);
        }

        // ✅ 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenExcetion(AuthErrorCode.No_Member));

        // ✅ (중요) 연관 데이터 때문에 hard delete 실패할 수 있음
        // 2) hard delete면 cascade/orphanRemoval / FK on delete cascade 정리가 필요
        memberRepository.delete(member);

        // ✅ refresh_token 쿠키 삭제 내려주기
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);       // https 운영이면 true 유지
        cookie.setPath("/");
        cookie.setMaxAge(0);          // 삭제
        response.addCookie(cookie);

        return ApiResponse.onSuccess(
                AuthSuccessCode.Auth_delete_Suc,
                LogoutResponse.builder().logoutSuccess(true).deleteSuccess(true).build()
        );
    }


}
