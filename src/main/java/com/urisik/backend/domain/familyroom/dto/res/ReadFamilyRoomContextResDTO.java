package com.urisik.backend.domain.familyroom.dto.res;

import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;
import com.urisik.backend.domain.member.enums.FamilyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadFamilyRoomContextResDTO {

    private Long familyRoomId;
    private FamilyPolicy familyPolicy;
    private Me me;
    private Capabilities capabilities;
    private boolean mealPlanCreated;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Me {
        private Long memberId;
        private FamilyRole familyRole;   // e.g. "MOM"
        private String nickName;     // e.g. "김엄마"
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Capabilities {
        private boolean leader;
        private boolean canEditWishlist;
        private boolean canCreateMealPlan;
        private boolean canEditMealPlan;
    }
}
