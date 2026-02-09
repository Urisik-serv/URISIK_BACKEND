package com.urisik.backend.domain.recipe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transformed_recipe_step_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransformedRecipeStepImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transformedRecipeId;

    private int stepOrder;

    @Column(nullable = false)
    private String imageUrl;

    public TransformedRecipeStepImage(Long transformedRecipeId, int stepOrder, String imageUrl) {
        this.transformedRecipeId = transformedRecipeId;
        this.stepOrder = stepOrder;
        this.imageUrl = imageUrl;
    }
}
