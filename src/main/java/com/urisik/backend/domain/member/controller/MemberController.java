package com.urisik.backend.domain.member.controller;


import com.urisik.backend.domain.member.dto.req.MemberRequest;
import com.urisik.backend.domain.member.dto.res.MemberResponse;
import com.urisik.backend.domain.member.exception.code.MemberSuccessCode;
import com.urisik.backend.domain.member.service.MemberService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(name = "Member", description = "유저 관련 API")
public class MemberController {

    private final MemberService memberService;

    @PatchMapping("/agree")
    public ApiResponse<MemberResponse.PatchAgree> agree(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberRequest.PatchAgree req
    ) {
        MemberResponse.PatchAgree res = memberService.patchAgree(memberId, req);
        return ApiResponse.onSuccess(MemberSuccessCode.AGREEMENT_UPDATE, res);
    }

    @GetMapping("/alarm")
    public ApiResponse<MemberResponse.alarmInfo> alarmInfo(
            @AuthenticationPrincipal Long memberId
    ){
        MemberResponse.alarmInfo res = memberService.getAlarmInfo(memberId);
        return ApiResponse.onSuccess(MemberSuccessCode.AlARM_GET, res);
    }

    @PatchMapping("/alarm")
    public ApiResponse<MemberResponse.alarmInfo> alarmUpdateInfo(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberRequest.AlarmUpdateInfo req
    ){
        MemberResponse.alarmInfo res = memberService.updateAlarmInfo(memberId, req);
        return ApiResponse.onSuccess(MemberSuccessCode.AlARM_UPDATE, res);
    }


}
