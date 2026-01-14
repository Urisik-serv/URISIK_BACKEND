package com.urisik.backend.domain.familyroom.dto.req;

import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;

public record CreateFamilyRoomReqDTO(
        String familyName,
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
