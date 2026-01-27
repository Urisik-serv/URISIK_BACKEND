package com.urisik.backend.domain.member.controller;


import com.urisik.backend.domain.member.dto.req.MemberRequest;
import com.urisik.backend.domain.member.dto.res.MemberResponse;
import com.urisik.backend.domain.member.service.MemberService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {



    private final MemberService memberService;

    @PatchMapping("/agree")
    public ApiResponse<MemberResponse.PatchAgree> agree(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberRequest.PatchAgree req
    ) {
        MemberResponse.PatchAgree res = memberService.patchAgree(memberId, req);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, res);
    }

    @GetMapping("/alarm")
    public ApiResponse<MemberResponse.alarmInfo> alarmInfo(
            @AuthenticationPrincipal Long memberId
    ){




    }

    @PatchMapping("/alarm")
    public ApiResponse<MemberResponse.alarmInfo> alarmUpdateInfo(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberRequest.PatchAgree req
    ){




    }



}
