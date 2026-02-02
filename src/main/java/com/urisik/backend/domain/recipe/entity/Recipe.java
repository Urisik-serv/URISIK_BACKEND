package com.urisik.backend.domain.recipe.entity;

import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.recipe.enums.SourceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // 외부 API 원본 재료 문자열 그대로
    @Lob
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String ingredientsRaw;

    // 외부 API 단계들을 합친 원본 조리법 문자열 그대로
    @Lob
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String instructionsRaw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    @Column(nullable = false)
    private int reviewCount = 0;

    //위시리스트 갯수
    @Column(nullable = false)
    private int wishCount = 0;


    @Column(nullable = false)
    private double avgScore = 0.0;

    // 외부 API RCP_SEQ 또는 AI 요청 ID
    @Column(length = 255)
    private String sourceRef;

    public Recipe(
            String title,
            String ingredientsRaw,
            String instructionsRaw,
            SourceType sourceType,
            String sourceRef
    ) {
        this.title = title;
        this.ingredientsRaw = ingredientsRaw;
        this.instructionsRaw = instructionsRaw;
        this.sourceType = sourceType;
        this.sourceRef = sourceRef;
    }

    /** 리뷰 개수 증가 */
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


