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

    @Column
    private String foodName;
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "newfood_id", nullable = false)
    private NewFood newFood;
    */

    public static MemberWishList of(String foodName) {
        MemberWishList w = new MemberWishList();
        w.foodName = foodName;
        return w;
    }

    public void setFamilyMemberProfile(FamilyMemberProfile profile) {
        this.familyMemberProfile = profile;
    }
}


