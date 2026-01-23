package com.urisik.backend.domain.member.entity;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.member.enums.FamilyRole;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "family_member_profile")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyMemberProfile extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyRole familyRole;

    @Lob
    @Column(name = "liked_ingredients")
    private String likedIngredients;

    @Lob
    @Column(name = "disliked_ingredients")
    private String dislikedIngredients;

    @Column(name = "profile_pic_url")
    private String profilePicUrl;


    /*
    N:1연관
    */
    //가족방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room")
    private FamilyRoom familyRoom;


    /*
    1:N 연관
    */


    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MemberAllergy> memberAllergyList= new ArrayList<>();

    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MemberWishList> memberWishLists = new ArrayList<>();

    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DietPreference> dietPreferenceList = new ArrayList<>();

    // 1:1 연관
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member",nullable = false, unique = true)
    private Member member;



    public void addAllergy(MemberAllergy allergy) {
        memberAllergyList.add(allergy);
        allergy.setFamilyMemberProfile(this);
    }

    public void addWish(MemberWishList wish) {
        memberWishLists.add(wish);
        wish.setFamilyMemberProfile(this);
    }

    public void addDietPreference(DietPreference dietPreference) {
        dietPreferenceList.add(dietPreference);
        dietPreference.setFamilyMemberProfile(this);
    }
}

