package com.urisik.backend.domain.member.converter;

import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.entity.DietPreference;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.enums.DietPreferenceList;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class FamilyMemberProfileConverter {


    public static FamilyMemberProfileResponse.Create toCreate(FamilyMemberProfile profile) {

        List<Allergen> allergies = profile.getMemberAllergyList().stream()
                .map(MemberAllergy::getAllergen)
                .toList();

        List<String> wishItems = profile.getMemberWishLists().stream()
                .map(MemberWishList::getFoodName)
                .toList();

        // enum을 무엇으로 내려줄지 선택:
        // 1) diet.getDietPreference().name()  -> "KOREAN"
        // 2) diet.getDietPreference().getDisplayName() -> "한식"
        List<DietPreferenceList> dietPreferences = profile.getDietPreferenceList().stream()
                .map(DietPreference::getDietPreference) // ✅ enum
                .toList();

        return FamilyMemberProfileResponse.Create.builder()
                .isSuccess(true)
                .nickname(profile.getNickname())
                .role(profile.getRole())
                .likedIngredients(profile.getLikedIngredients())
                .dislikedIngredients(profile.getDislikedIngredients())
                .allergy(allergies)
                .wishItems(wishItems)
                .dietPreferences(dietPreferences)
                .build();
    }

    public static FamilyMemberProfileResponse.Detail toDetail(FamilyMemberProfile profile){

        List<Allergen> allergies = profile.getMemberAllergyList().stream()
                .map(MemberAllergy::getAllergen)
                .toList();

        List<String> wishItems = profile.getMemberWishLists().stream()
                .map(MemberWishList::getFoodName)
                .toList();

        // enum을 무엇으로 내려줄지 선택:
        // 1) diet.getDietPreference().name()  -> "KOREAN"
        // 2) diet.getDietPreference().getDisplayName() -> "한식"
        List<DietPreferenceList> dietPreferences = profile.getDietPreferenceList().stream()
                .map(DietPreference::getDietPreference) // ✅ enum
                .toList();

        return FamilyMemberProfileResponse.Detail.builder()
                .isSuccess(true)
                .nickname(profile.getNickname())
                .role(profile.getRole())
                .likedIngredients(profile.getLikedIngredients())
                .dislikedIngredients(profile.getDislikedIngredients())
                .allergy(allergies)
                .wishItems(wishItems)
                .dietPreferences(dietPreferences)
                .build();



    }
}
