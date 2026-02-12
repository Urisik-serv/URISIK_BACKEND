package com.urisik.backend.domain.member.entity;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.member.enums.AlarmPolicy;
import com.urisik.backend.domain.member.enums.DietPreferenceList;
import com.urisik.backend.domain.member.enums.FamilyRole;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.review.entity.Review;
import com.urisik.backend.domain.review.entity.TransformedRecipeReview;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "family_member_profile")
@Getter
@Setter
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

    @Builder.Default
    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MemberAllergy> memberAllergyList= new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MemberWishList> memberWishLists = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MemberTransformedRecipeWish> memberTransformedWishLists = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DietPreference> dietPreferenceList = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Review> reviewList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "familyMemberProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TransformedRecipeReview> transformedRecipeReviewsList = new ArrayList<>();

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
    //변형된 레시피 추가.
    public void addTransWish(MemberTransformedRecipeWish wish) {
        memberTransformedWishLists.add(wish);
        wish.setFamilyMemberProfile(this);
    }


    public void replaceAllergies(List<Allergen> allergens) {
        this.memberAllergyList.clear();   // 기존 전부 삭제(orphanRemoval)
        if (allergens == null) return;

        for (Allergen a : allergens) {
            this.addAllergy(MemberAllergy.of(a)); // ✅ addAllergy가 profile 세팅함
        }
    }

    public void replaceWishItems(List<Recipe> recipes) {
        this.memberWishLists.clear();
        if (recipes == null) return;

        for (Recipe recipe : recipes) {
            this.addWish(MemberWishList.of(recipe));
        }
    }

    public void replaceDietPreferences(List<DietPreferenceList> diets) {
        this.dietPreferenceList.clear();
        if (diets == null) return;

        for (DietPreferenceList d : diets) {
            this.addDietPreference(DietPreference.of(d));
        }
    }






}

