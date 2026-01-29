package com.urisik.backend.domain.review.entity;

import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transformed_recipe_review")
@Builder
@AllArgsConstructor
public class TransformedRecipeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 별점
    @Column(nullable = false)
    private Integer score;   // 1 ~ 5 범위의 정수

    // 취향
    @Column(nullable = false)
    private boolean isFavorite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_id")
    private FamilyMemberProfile familyMemberProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformed_recipe_id")
    private TransformedRecipe transformedRecipe;

}