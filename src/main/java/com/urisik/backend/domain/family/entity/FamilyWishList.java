package com.urisik.backend.domain.family.entity;

import com.urisik.backend.global.apiPayload.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_wishlist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FamilyWishList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    @Column(name = "source_profile_id")
    private SoureProfile sourceProfileId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;


}
