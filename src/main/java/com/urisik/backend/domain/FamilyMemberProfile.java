package com.urisik.backend.domain;

import com.urisik.backend.domain.enums.FamilyRole;
import com.urisik.backend.domain.member.Member;
import com.urisik.backend.global.apiPayload.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;


public class FamilyMemberProfile extends BaseEntity {

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
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "family_room")
        private FamilyRoom familyRoom;
         */


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private Member user;


    }
}
