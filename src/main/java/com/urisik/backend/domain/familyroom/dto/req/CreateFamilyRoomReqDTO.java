package com.urisik.backend.domain.familyroom.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateFamilyRoomReqDTO(
        FamilyComposition familyComposition,
        FamilyPolicy familyPolicy
) {
    public record FamilyComposition(
            boolean hasMother,
            boolean hasFather,
            boolean hasGrandMother,
            boolean hasGrandFather,
            int sonCount,
            int daughterCount
    ) {}
}
