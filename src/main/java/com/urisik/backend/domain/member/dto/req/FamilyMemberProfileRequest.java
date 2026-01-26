package com.urisik.backend.domain.member.dto.req;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.enums.FamilyRole;
import com.urisik.backend.domain.member.enums.DietPreferenceList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class FamilyMemberProfileRequest {

    @Getter
    @NoArgsConstructor
    public static class Create {

        @NotBlank
        private String nickname;

        @NotNull
        private FamilyRole role;

        private String likedIngredients;
        private String dislikedIngredients;
        @NotEmpty(message = "알러지는 최소 1개 이상 선택해야 합니다")
        private List<Allergen> allergy;

        private List<String> wishItems;

        @NotEmpty(message = "식단 선호도는 최소 1개 이상 선택해야 합니다")
        private List<DietPreferenceList> dietPreferences;
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

        @NotEmpty(message = "알러지는 최소 1개 이상 선택해야 합니다")
        private List<Allergen> allergy;
        private List<String> wishItems;
        @NotEmpty(message = "식단 선호도는 최소 1개 이상 선택해야 합니다")
        private List<DietPreferenceList> dietPreferences;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdatePic {
        @NotBlank
        private String profilePicUrl;
    }



}