package com.urisik.backend.domain.member.entity;

import com.urisik.backend.domain.familyroom.enums.FamilyRole;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "family_member_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FamilyMemberProfile extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyRole role;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_room")
    private FamilyRoom familyRoom;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /*
    1:N 연관
    */

    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAllergy> memberAllergyList = new ArrayList<>();

    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberWishList> memberWishLists = new ArrayList<>();

    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DietPreference> dietPreferenceList = new ArrayList<>();


}

