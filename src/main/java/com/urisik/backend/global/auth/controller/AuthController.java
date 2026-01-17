package com.urisik.backend.global.auth.controller;

import com.urisik.backend.domain.member.entity.Member;
import com.urisik.backend.domain.member.repo.MemberRepository;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.code.GeneralSuccessCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import com.urisik.backend.global.auth.dto.AccessTokenDto;
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
    private final MemberRepository memberRepository;

    @PostMapping("/reissue")
    public ApiResponse<AccessTokenDto> reissue(
            @CookieValue(value = "refresh_token" , required = false) String refreshToken) {



        // 비어있거나, 유효하거나 , 리프레시 토큰(어쎄스로 속일수도) 이어야함
        if (refreshToken == null||!jwtUtil.isValid(refreshToken) || !jwtUtil.isRefresh(refreshToken)) {
            throw new GeneralException(GeneralErrorCode.VALIDATION_ERROR);
        }

        Long memberId = jwtUtil.getMemberId(refreshToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.VALIDATION_ERROR));

        String accessToken = jwtUtil.createAccessToken(memberId, member.getRole());

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, new AccessTokenDto(accessToken));
    }

}
