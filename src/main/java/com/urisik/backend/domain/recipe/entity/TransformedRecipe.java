package com.urisik.backend.domain.recipe.entity;

import com.urisik.backend.domain.recipe.enums.ValidationStatus;
import com.urisik.backend.domain.recipe.enums.Visibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transformed_recipe",
        indexes = {
                @Index(name = "idx_tr_recipe", columnList = "recipe_id"),
                @Index(name = "idx_tr_family", columnList = "family_room_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransformedRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기준 원본 레시피
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    private String title;

    // FamilyRoom 엔티티를 직접 연결해도 되지만, 구조상 profile->familyRoom 이 있고, familyRoomId만 써도 충분해서 FK ID만 저장하는 방식으로 구현
    @Column(name = "family_room_id", nullable = false)
    private Long familyRoomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    // 서버가 감지한 알레르기 목록(JSON string)
    @Lob
    private String detectedAllergens;

    // 서버가 결정한 대체규칙(대체재/이유)(JSON string)
    @Lob
    private String substitutions;

    // AI가 재작성한 결과
    @Lob
    @Column(nullable = false)
    private String ingredientsTransformed;

    @Lob
    @Column(nullable = false)
    private String instructionsTransformed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationStatus validationStatus = ValidationStatus.VALID;

    @Column(nullable = false)
    private int reviewCount = 0;

    //위시리스트 선택개수
    @Column(nullable = false)
    private int wishCount = 0;

    @Column(nullable = false)
    private double avgScore = 0.0;

    private TransformedRecipe(
            Recipe recipe,
            Long familyRoomId,
            Visibility visibility,
            String detectedAllergens,
            String substitutions,
            String ingredientsTransformed,
            String instructionsTransformed,
            ValidationStatus validationStatus
    ) {
        this.recipe = recipe;
        this.familyRoomId = familyRoomId;
        this.visibility = visibility;
        this.detectedAllergens = detectedAllergens;
        this.substitutions = substitutions;
        this.ingredientsTransformed = ingredientsTransformed;
        this.instructionsTransformed = instructionsTransformed;
        this.validationStatus = validationStatus;
    }

    public static TransformedRecipe createPublicValid(
            Recipe recipe,
            Long familyRoomId,
            String detectedAllergensJson,
            String substitutionsJson,
            String ingredientsTransformed,
            String instructionsTransformed
    ) {
        return new TransformedRecipe(
                recipe,
                familyRoomId,
                Visibility.PUBLIC,
                detectedAllergensJson,
                substitutionsJson,
                ingredientsTransformed,
                instructionsTransformed,
                ValidationStatus.VALID
        );
    }

    public void updateReviewCount() {
        this.reviewCount++;
    }

    /** 평균 점수 갱신 */
    public void updateAvgScore(int newScore) {
        this.avgScore =
                ((this.avgScore * (this.reviewCount - 1)) + newScore)
                        / this.reviewCount;
    }

    public void incrementWishCount() {
        wishCount++;
    }

    public void decrementWishCount() {
        wishCount--;
    }


}

