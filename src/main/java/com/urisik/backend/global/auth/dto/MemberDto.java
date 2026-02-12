package com.urisik.backend.global.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberDto {

    private Long id;
    private String role;
    private String name;
    private String credentialId;
}