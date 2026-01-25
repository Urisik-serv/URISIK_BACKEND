package com.urisik.backend.domain.familyroom.dto.res;

import java.util.List;

/**
 * 가족 위시리스트 조회 응답 DTO
 * TODO(리팩터링):
 * - NewFood 연동 시: newFoodName/foodImageUrl/score/foodCategory는 NewFood에서 조회해서 채운다.
 * - 개인 위시리스트 연동 시: sourceProfile.profiles는 누가 위시했는지를 개인 위시리스트 집계로 채운다.
 */
public class FamilyWishListItemResDTO {

    private Long newFoodId;
    private String newFoodName;
    private String foodImageUrl;
    private Double score;
    private FoodCategory foodCategory;
    private SourceProfile sourceProfile;

    public FamilyWishListItemResDTO(
            Long newFoodId,
            String newFoodName,
            String foodImageUrl,
            Double score,
            FoodCategory foodCategory,
            SourceProfile sourceProfile
    ) {
        this.newFoodId = newFoodId;
        this.newFoodName = newFoodName;
        this.foodImageUrl = foodImageUrl;
        this.score = score;
        this.foodCategory = foodCategory;
        this.sourceProfile = sourceProfile;
    }

    public Long getNewFoodId() {
        return newFoodId;
    }

    public String getNewFoodName() {
        return newFoodName;
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

    public SourceProfile getSourceProfile() {
        return sourceProfile;
    }

    public static class FoodCategory {
        private String code;
        private String label;

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
        private List<Profile> profiles;

        public SourceProfile(List<Profile> profiles) {
            this.profiles = profiles;
        }

        public List<Profile> getProfiles() {
            return profiles;
        }
    }

    /**
     * 개인 프로필 DTO
     */
    public static class Profile {
        private Long profileId;
        private String name;

        public Profile(Long profileId, String name) {
            this.profileId = profileId;
            this.name = name;
        }

        public Long getProfileId() {
            return profileId;
        }

        public String getName() {
            return name;
        }
    }
}
