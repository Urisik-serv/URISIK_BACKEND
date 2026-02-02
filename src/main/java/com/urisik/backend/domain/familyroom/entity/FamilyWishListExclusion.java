package com.urisik.backend.domain.familyroom.entity;

import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "family_wishlist_exclusion",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_family_recipe", columnNames = {"family_room_id", "recipe_id"}),
                @UniqueConstraint(name = "uk_family_transformed", columnNames = {"family_room_id", "transformed_recipe_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FamilyWishListExclusion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "transformed_recipe_id")
    private Long transformedRecipeId;

    @PrePersist
    @PreUpdate
    private void validateExactlyOneTarget() {
        boolean hasRecipe = recipeId != null;
        boolean hasTransformed = transformedRecipeId != null;
        if (hasRecipe == hasTransformed) {
            throw new IllegalStateException("Exactly one of recipeId or transformedRecipeId must be set");
        }
    }

    public static FamilyWishListExclusion ofCanonical(FamilyRoom familyRoom, Long recipeId) {
        return FamilyWishListExclusion.builder()
                .familyRoom(familyRoom)
                .recipeId(recipeId)
                .transformedRecipeId(null)
                .build();
    }

    public static FamilyWishListExclusion ofTransformed(FamilyRoom familyRoom, Long transformedRecipeId) {
        return FamilyWishListExclusion.builder()
                .familyRoom(familyRoom)
                .recipeId(null)
                .transformedRecipeId(transformedRecipeId)
                .build();
    }
}
