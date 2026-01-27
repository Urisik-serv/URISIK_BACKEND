package com.urisik.backend.domain.familyroom.dto.res;

import java.util.List;

/**
 * 가족 위시리스트 조회 응답 DTO
 * - 개인 위시리스트를 가족방 단위로 집계한 결과를 표현
*/
public class FamilyWishListItemResDTO {

    /**
     * TODO(리팩터링):
     * - recipeId/recipeName -> newFoodId/newFoodName 로 치환 예정
     * - foodImageUrl/score/foodCategory는 NewFood(또는 Recipe 확장 정보)에서 채운다.
     */
    private final Long recipeId;
    private final String recipeName;
    private final String foodImageUrl;
    private final Double score;
    private final FoodCategory foodCategory;
    /**
     * 주간식단 후보로 사용 가능 여부 (가족 전체 기준)
     * TODO(리팩터링):
     * - NewFood/Recipe 알레르기 성분 + 가족 구성원 알레르기 집계로 계산해 채운다.
     * - 저장하지 않고 조회 시점에 계산해서 내려주는 값이다.
     */
    private final boolean usableForMealPlan;

    // 이 레시피를 위시한 사람들(개인 위시리스트 집계 결과)
    private final SourceProfile sourceProfile;

    public FamilyWishListItemResDTO(
            Long recipeId,
            String recipeName,
            String foodImageUrl,
            Double score,
            FoodCategory foodCategory,
            boolean usableForMealPlan,
            SourceProfile sourceProfile
    ) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.foodImageUrl = foodImageUrl;
        this.score = score;
        this.foodCategory = foodCategory;
        this.usableForMealPlan = usableForMealPlan;
        this.sourceProfile = sourceProfile;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public String getFoodImageUrl() {
        return foodImageUrl;
    }

    public Double getScore() {
        return score;
    }

    public FoodCategory getFoodCategory() {
        return foodCategory;
    }

    public boolean isUsableForMealPlan() {
        return usableForMealPlan;
    }

    public SourceProfile getSourceProfile() {
        return sourceProfile;
    }

    public static class FoodCategory {
        private final String code;
        private final String label;

        public FoodCategory(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class SourceProfile {
        // 이 레시피를 위시한 가족 구성원 목록
        private final List<Profile> profiles;

        public SourceProfile(List<Profile> profiles) {
            this.profiles = profiles;
        }

        public List<Profile> getProfiles() {
            return profiles;
        }
    }

    /**
     * 개인 프로필 요약 DTO
     * - 개인 위시리스트 집계 결과로 채워진다.
     */
    public static class Profile {
        private final Long profileId;
        private final String nickname;

        public Profile(Long profileId, String nickname) {
            this.profileId = profileId;
            this.nickname = nickname;
        }

        public Long getProfileId() {
            return profileId;
        }

        public String getNickname() {
            return nickname;
        }
    }
}
