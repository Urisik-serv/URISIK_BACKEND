package com.urisik.backend.domain.recipe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe_external_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecipeExternalMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1로 두고, recipe가 주인(FK를 metadata가 가지는 형태)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false, unique = true)
    private Recipe recipe;

    private String category;        // 요리종류
    private String servingWeight;   // 중량(1인분)

    private Integer calorie;
    private Integer carbohydrate;
    private Integer protein;
    private Integer fat;
    private Integer sodium;

    private String imageSmallUrl;
    private String imageLargeUrl;

    public RecipeExternalMetadata(
            Recipe recipe,
            String category,
            String servingWeight,
            Integer calorie,
            Integer carbohydrate,
            Integer protein,
            Integer fat,
            Integer sodium,
            String imageSmallUrl,
            String imageLargeUrl
    ) {
        this.recipe = recipe;
        this.category = category;
        this.servingWeight = servingWeight;
        this.calorie = calorie;
        this.carbohydrate = carbohydrate;
        this.protein = protein;
        this.fat = fat;
        this.sodium = sodium;
        this.imageSmallUrl = imageSmallUrl;
        this.imageLargeUrl = imageLargeUrl;
    }

}
