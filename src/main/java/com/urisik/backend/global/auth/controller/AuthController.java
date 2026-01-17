package com.urisik.backend.global.auth.controller;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.GeneralException;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.auth.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/reissue")
    public ApiResponse<AccessTokenResponse> reissue(@CookieValue("refresh_token") String refreshToken) {

        if (!jwtUtil.isValid(refreshToken) || !jwtUtil.isRefresh(refreshToken)) {
            throw new GeneralException(GeneralErrorCode.VALIDATION_ERROR);
        }

        Long memberId = jwtUtil.getMemberId(refreshToken);

        // role은 토큰에 없으니 DB 조회하거나, refresh에도 role 넣거나(보통 DB 조회)
        String role = memberService.getRole(memberId);

        String accessToken = jwtUtil.createAccessToken(memberId, role);

        return ApiResponse.onSuccess(new AccessTokenResponse(accessToken));
    }

}
