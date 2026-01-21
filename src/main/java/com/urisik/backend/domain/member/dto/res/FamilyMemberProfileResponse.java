package com.urisik.backend.domain.member.dto.res;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.familyroom.enums.FamilyRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class FamilyMemberProfileResponse {

    @Getter
    @NoArgsConstructor
    public static class Create {

        private String nickname;

        private FamilyRole role;

        private String likedIngredients;
        private String dislikedIngredients;

        private List<Allergen> allergy;
        private List<String> wishItems;
        private List<String> dietPreferences;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {

        // Update는 보통 부분수정(PATCH) 고려해서 NotBlank/NotNull을 빼는 게 일반적
        // (PUT으로 전체교체 할 거면 Create와 동일하게 필수 제약 걸어도 됨)

        private String nickname;
        private FamilyRole role;

        private String likedIngredients;
        private String dislikedIngredients;

        private List<Allergen> allergy;
        private List<String> wishItems;
        private List<String> dietPreferences;
    }
}
