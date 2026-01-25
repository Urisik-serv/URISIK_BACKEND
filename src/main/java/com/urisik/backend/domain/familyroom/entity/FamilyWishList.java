package com.urisik.backend.domain.familyroom.entity;

import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "family_wishlist",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_family_room_food",
                        columnNames = {"family_room_id", "food_id"}
                )
        },
        indexes = {
                @Index(name = "idx_family_room_id", columnList = "family_room_id"),
                @Index(name = "idx_food_id", columnList = "food_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FamilyWishList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;

    private FamilyWishList(FamilyRoom familyRoom, Long foodId) {
        this.familyRoom = familyRoom;
        this.foodId = foodId;
    }

    /**
     * 가족 위시리스트에 음식 포함
     */
    public static FamilyWishList include(FamilyRoom familyRoom, Long foodId) {
        return new FamilyWishList(familyRoom, foodId);
    }
}
