package com.urisik.backend.domain.member.dto.res;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.enums.AlarmPolicy;
import com.urisik.backend.domain.member.enums.FamilyRole;
import com.urisik.backend.domain.member.enums.DietPreferenceList;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

public class FamilyMemberProfileResponse {

    @Getter
    @Builder
    public static class Create {

        private Boolean isSuccess;
        private String nickname;

        private FamilyRole role;

        private String likedIngredients;
        private String dislikedIngredients;

        private List<Allergen> allergy;

        private List<DietPreferenceList> dietPreferences;
    }


    @Getter
    @Builder
    public static class Update {

        // Update는 보통 부분수정(PATCH) 고려해서 NotBlank/NotNull을 빼는 게 일반적
        // (PUT으로 전체교체 할 거면 Create와 동일하게 필수 제약 걸어도 됨)

        private Boolean isSuccess;
        private String nickname;
        private FamilyRole role;

        private String likedIngredients;
        private String dislikedIngredients;

        private List<Allergen> allergy;

        private List<DietPreferenceList> dietPreferences;
    }


    @Getter
    @Builder
    public static class UpdatePic {

        private Boolean isSuccess;
        private String profilePicUrl;
    }

    @Getter
    @Builder
    public static class Detail {
        
        private Boolean isSuccess;
        private Long profileId;
        private String nickname;
        private FamilyRole role;

        private String likedIngredients;
        private String dislikedIngredients;
        private String profilePicUrl;

        private List<Allergen> allergy;

        private List<DietPreferenceList> dietPreferences;
    }


    @Getter
    @Builder
    public static class Delete{
        private Boolean isSuccess;
    }

    @Getter
    @Builder
    public static class getFamilyProfilesResponse {

        private Boolean isSuccess;
        private List<familyDetail> familyDetails;

    }

    @Getter
    @Builder
    public static class familyDetail {

        private Long profileId;
        private String nickname;
        private FamilyRole role;
        private String profilePicUrl;

    }








}
