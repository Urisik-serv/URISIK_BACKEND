package com.urisik.backend.domain.member.entity;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "personal_wishList")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberWishList extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_member_profile_id", nullable = false)
    private FamilyMemberProfile familyMemberProfile;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newfood_id", nullable = false)
    private NewFood newFood;
    */

}


