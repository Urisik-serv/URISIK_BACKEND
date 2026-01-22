package com.urisik.backend.domain.familyroom.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReadInviteResDTO {

    private Long familyRoomId;
    private String inviterName;
    private LocalDateTime expiresAt;
    private boolean isExpired;
}
