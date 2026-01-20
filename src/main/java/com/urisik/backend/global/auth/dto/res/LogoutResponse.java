package com.urisik.backend.global.auth.dto.res;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class LogoutResponse {
    private boolean success;
}
