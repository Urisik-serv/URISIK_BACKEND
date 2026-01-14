package com.urisik.backend.domain.entity;

import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "already_cooked_food")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlreadyCookedFood extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    @Column(name = "newfood_id", nullable = false)
    private NewFood newfoodId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;


}
