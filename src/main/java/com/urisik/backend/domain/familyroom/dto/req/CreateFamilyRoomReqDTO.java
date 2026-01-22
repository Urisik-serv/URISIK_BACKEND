package com.urisik.backend.domain.familyroom.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;

@JsonIgnoreProperties(ignoreUnknown = true) // familySize 수정되어도 서버가 모르는 값 무시
public record CreateFamilyRoomReqDTO(
        Integer familySize,
        FamilyComposition familyComposition,
        FamilyPolicy familyPolicy
) {
    public record FamilyComposition(
            boolean hasMother,
            boolean hasFather,
            int sonCount,
            int daughterCount
    ) {}
}
