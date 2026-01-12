package com.urisik.backend.domain.family.entity;

import com.urisik.backend.global.apiPayload.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FamilyRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;


}
