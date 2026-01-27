package com.urisik.backend.domain.familyroom.entity;

import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "family_wishlist_exclusion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_family_room_recipe",
                        columnNames = {"family_room_id", "recipe_id"}
                )
        },
        indexes = {
                @Index(name = "idx_fwe_family_room_id", columnList = "family_room_id"),
                @Index(name = "idx_fwe_recipe_id", columnList = "recipe_id")
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

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    public static FamilyWishListExclusion of(FamilyRoom familyRoom, Long recipeId) {
        return FamilyWishListExclusion.builder()
                .familyRoom(familyRoom)
                .recipeId(recipeId)
                .build();
    }
}
