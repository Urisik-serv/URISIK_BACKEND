package com.urisik.backend.domain.familyroom.entity;

import com.urisik.backend.domain.familyroom.enums.FamilyPolicy;
import com.urisik.backend.domain.familyroom.enums.FamilyRole;
import com.urisik.backend.domain.familyroom.enums.FamilyStatus;
import com.urisik.backend.domain.member.entity.Member;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_room_id", nullable = false)
    private FamilyRoom familyRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

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

    private FamilyMember(FamilyRoom familyRoom, Member member, FamilyPolicy familyPolicy) {
        this.familyRoom = familyRoom;
        this.member = member;
        this.familyPolicy = familyPolicy;
        this.status = FamilyStatus.ACTIVE;
        this.familyRole = null; // 프로필 생성 전에는 null 허용
    }

    public static FamilyMember createMember(FamilyRoom familyRoom, Member member, FamilyPolicy policy) {
        return new FamilyMember(familyRoom, member, policy);
    }

    /**
     * 초대 토큰을 통해 가족방에 참여하는 경우 (프로필 미완료 상태)
     */
    public static FamilyMember createInvitedMember(
            FamilyRoom familyRoom,
            Member member,
            FamilyPolicy familyPolicy
    ) {
        FamilyMember fm = new FamilyMember(familyRoom, member, familyPolicy);
        fm.familyRole = null; // 역할 미확정
        return fm;
    }

    public Long getFamilyRoomId() {
        return familyRoom.getId();
    }

    public Long getMemberId() {
        return member.getId();
    }
}
