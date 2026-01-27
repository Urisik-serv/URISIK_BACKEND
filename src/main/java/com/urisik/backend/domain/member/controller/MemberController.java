package com.urisik.backend.domain.member.controller;


import com.urisik.backend.domain.member.dto.req.MemberRequest;
import com.urisik.backend.global.apiPayload.ApiResponse;
import com.urisik.backend.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {


    @PatchMapping("/agree")
    public ApiResponse<MemberRequest.PatchAgree> agreeMent(

    ){

        MemberRequest.PatchAgree req = new MemberRequest.PatchAgree();

        return ApiResponse.onSuccess(GeneralSuccessCode.OK,req);
    }



}
