package com.urisik.backend.domain.familyroom.dto.res;

import java.util.List;

/**
 * 가족 위시리스트 조회 응답 DTO
 * - 개인 위시리스트를 가족방 단위로 집계한 결과를 표현
*/
public class FamilyWishListItemResDTO {

    private final String type; // RECIPE | TRANSFORMED_RECIPE
    private final Long id;
    private final String title;
    private final String imageUrl;
    private final Double avgScore;
    private final String allergyStatus;
    private final Category category;
    private final String ingredientsRaw;
    private final SourceProfile sourceProfile; // 이 레시피를 위시한 가족원 (개인 위시리스트 집계 결과)

    public FamilyWishListItemResDTO(
            String type,
            Long id,
            String title,
            String imageUrl,
            Double avgScore,
            String allergyStatus,
            Category category,
            String ingredientsRaw,
            SourceProfile sourceProfile
    ) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.avgScore = avgScore;
        this.allergyStatus = allergyStatus;
        this.category = category;
        this.ingredientsRaw = ingredientsRaw;
        this.sourceProfile = sourceProfile;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getAvgScore() {
        return avgScore;
    }

    public String getAllergyStatus() {
        return allergyStatus;
    }

    public Category getCategory() {
        return category;
    }

    public String getIngredientsRaw() {
        return ingredientsRaw;
    }

    public SourceProfile getSourceProfile() {
        return sourceProfile;
    }

    public static class Category {
        private final String code;
        private final String label;

        public Category(String code, String label) {
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
        private final String profilePicUrl;

        public Profile(Long profileId, String nickname, String profilePicUrl) {
            this.profileId = profileId;
            this.nickname = nickname;
            this.profilePicUrl = profilePicUrl;
        }

        public Long getProfileId() {
            return profileId;
        }

        public String getNickname() {
            return nickname;
        }

        public String getProfilePicUrl() {
            return profilePicUrl;
        }
    }
}
