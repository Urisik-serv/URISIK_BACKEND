package com.urisik.backend.domain.member.converter;

import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.entity.MemberAllergy;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.member.entity.DietPreference;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.dto.res.FamilyMemberProfileResponse;
import com.urisik.backend.domain.member.enums.DietPreferenceList;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
public class FamilyMemberProfileConverter {


    public static FamilyMemberProfileResponse.Create toCreate(FamilyMemberProfile profile) {

        List<Allergen> allergies = profile.getMemberAllergyList().stream()
                .map(MemberAllergy::getAllergen)
                .toList();


        // enum을 무엇으로 내려줄지 선택:
        // 1) diet.getDietPreference().name()  -> "KOREAN"
        // 2) diet.getDietPreference().getDisplayName() -> "한식"
        List<DietPreferenceList> dietPreferences = profile.getDietPreferenceList().stream()
                .map(DietPreference::getDietPreference) // ✅ enum
                .toList();

        return FamilyMemberProfileResponse.Create.builder()
                .nickname(profile.getNickname())
                .role(profile.getFamilyRole())
                .likedIngredients(profile.getLikedIngredients())
                .dislikedIngredients(profile.getDislikedIngredients())
                .allergy(allergies)
                .dietPreferences(dietPreferences)
                .build();
    }

    public static FamilyMemberProfileResponse.Detail toDetail(
            FamilyMemberProfile profile,
            List<MemberAllergy> allergies,
            List<DietPreference> diets,
            List<AllergenAlternative> alternatives
    ) {
        List<DietPreferenceList> dietEnums = diets.stream()
                .map(DietPreference::getDietPreference)
                .toList();

        // ✅ allergen별로 대체재료 name 그룹핑
        Map<Allergen, List<String>> allergenToAltNames = alternatives.stream()
                .collect(Collectors.groupingBy(
                        AllergenAlternative::getAllergen,
                        Collectors.mapping(a -> a.getIngredient().getName(), Collectors.toList())
                ));

        // ✅ 최종 DTO 리스트 (알러지 없는 경우 빈 리스트)
        List<FamilyMemberProfileResponse.allergenAndAlterIngredient> allergyAndAlterIngredients =
                allergies.stream()
                        .map(MemberAllergy::getAllergen)
                        .distinct()
                        .map(allergen ->
                                FamilyMemberProfileResponse.allergenAndAlterIngredient.builder()
                                        .allergen(allergen)
                                        .alteredIngredients(
                                                allergenToAltNames.getOrDefault(allergen, List.of())
                                        )
                                        .build()
                        )
                        .toList();

        return FamilyMemberProfileResponse.Detail.builder()
                .profileId(profile.getId())
                .nickname(profile.getNickname())
                .profilePicUrl(profile.getProfilePicUrl())
                .role(profile.getFamilyRole())
                .likedIngredients(profile.getLikedIngredients())
                .dislikedIngredients(profile.getDislikedIngredients())
                .dietPreferences(dietEnums)
                .allergyAndAlterIngredients(allergyAndAlterIngredients)
                .build();
    }


    public static FamilyMemberProfileResponse.Update toUpdate(
            FamilyMemberProfile profile,
            List<MemberAllergy> allergies,
            List<DietPreference> diets
    ) {
        List<Allergen> allergyEnums = allergies.stream()
                .map(MemberAllergy::getAllergen)
                .toList();

        List<DietPreferenceList> dietEnums = diets.stream()
                .map(DietPreference::getDietPreference)
                .toList();

        return FamilyMemberProfileResponse.Update.builder()
                .nickname(profile.getNickname())
                .role(profile.getFamilyRole())
                .likedIngredients(profile.getLikedIngredients())
                .dislikedIngredients(profile.getDislikedIngredients())
                .allergy(allergyEnums)
                .dietPreferences(dietEnums)
                .build();
    }
}
