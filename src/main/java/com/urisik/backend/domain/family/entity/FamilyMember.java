package com.urisik.backend.domain.family.entity;

import com.urisik.backend.domain.family.enums.FamilyRole;
import com.urisik.backend.domain.family.enums.GuaranteeType;
import com.urisik.backend.domain.family.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_role", nullable = false, length = 30)
    private FamilyRole familyRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "guaranteetype", nullable = false, length = 30)
    private GuaranteeType guaranteeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 30)
    private MemberStatus memberStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;


}
