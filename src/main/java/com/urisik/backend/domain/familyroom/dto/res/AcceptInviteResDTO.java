package com.urisik.backend.domain.familyroom.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AcceptInviteResDTO {
    private Long familyRoomId;
}
