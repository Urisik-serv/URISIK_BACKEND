package com.urisik.backend.domain.familyroom.entity;

import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;
import com.urisik.backend.domain.enums.FamilyRole;
import com.urisik.backend.domain.familyroom.enums.FamilyStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_family_room_member",
                columnNames = {"family_room_id", "member_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_room_id", nullable = false)
    private Long familyRoomId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // 프로필 생성 전에는 null 허용
    @Enumerated(EnumType.STRING)
    @Column(name = "family_role")
    private FamilyRole familyRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_policy", nullable = false)
    private FamilyPolicy familyPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FamilyStatus status;

    private FamilyMember(Long familyRoomId, Long memberId, FamilyPolicy familyPolicy) {
        this.familyRoomId = familyRoomId;
        this.memberId = memberId;
        this.familyPolicy = familyPolicy;
        this.status = FamilyStatus.ACTIVE;
        this.familyRole = null;
    }

    public static FamilyMember createOwner(Long familyRoomId, Long memberId, FamilyPolicy policy) {
        return new FamilyMember(familyRoomId, memberId, policy);
    }
}
