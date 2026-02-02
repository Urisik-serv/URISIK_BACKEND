package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.allergy.service.AllergySubstitutionService;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.repo.MemberWishListRepository;
import com.urisik.backend.domain.recipe.entity.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyWishListQueryService {

    private final MemberWishListRepository memberWishListRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;
    private final FamilyRoomService familyRoomService;

    // 가족방 전체 알레르기 집계
    private final MemberAllergyRepository memberAllergyRepository;

    // 알레르기 매칭 로직 재사용
    private final AllergySubstitutionService allergySubstitutionService;

    /**
     * 가족 위시리스트 조회
     * - 개인 위시리스트(MemberWishList)를 가족방 단위로 집계
     * - 방장이 삭제한 항목은 exclusion에 의해 조회에서만 제외
     * - 가족 알레르기 필터링:
     * 레시피 재료(ingredientsRaw) 기준으로 가족방 전체 알레르기(Allergen) 포함 여부를 판별
     * unsafe(알레르기 포함) 레시피는 가족 위시리스트 결과에서 제외
     */
    public List<FamilyWishListItemResDTO> getFamilyWishList(Long familyRoomId) {

        // 방장 제외 목록 조회 (exclusion)
        Set<Long> excludedRecipeIds =
                familyWishListExclusionRepository.findExcludedRecipeIdsByFamilyRoomId(familyRoomId);

        // 가족방 전체 알레르기 조회 (중복 제거)
        List<Allergen> familyAllergens =
                memberAllergyRepository.findDistinctAllergensByFamilyRoomId(familyRoomId);

        // 가족방 내 전체 개인 위시리스트 조회 (recipe + profile join fetch)
        List<MemberWishList> all = memberWishListRepository.findAllByFamilyRoomIdWithRecipe(familyRoomId);
        if (all == null || all.isEmpty()) return List.of();

        // exclusion 반영 + recipeId 기준 집계
        // - 인원 수는 distinct profile 기준
        // - 최신 기준은 max(wishId)
        Map<Long, Agg> grouped = new LinkedHashMap<>();

        for (MemberWishList w : all) {
            if (w == null || w.getRecipe() == null || w.getFamilyMemberProfile() == null) continue;

            Long recipeId = w.getRecipe().getId();
            if (recipeId == null) continue;

            if (excludedRecipeIds != null && excludedRecipeIds.contains(recipeId)) continue;

            Agg agg = grouped.computeIfAbsent(recipeId, id -> new Agg(w.getRecipe()));
            agg.add(w);
        }

        if (grouped.isEmpty()) return List.of();

        // 정렬
        // - 개인 위시리스트에 담은 인원 수 많은 순 (DESC)
        // - 최신순 (latest wish id DESC)
        List<Map.Entry<Long, Agg>> sortedEntries = new ArrayList<>(grouped.entrySet());

        sortedEntries.sort((e1, e2) -> {
            int cmp = Integer.compare(e2.getValue().getWisherCount(), e1.getValue().getWisherCount());
            if (cmp != 0) return cmp;
            return Long.compare(e2.getValue().getLatestWishId(), e1.getValue().getLatestWishId());
        });

        // DTO 변환
        List<FamilyWishListItemResDTO> result = new ArrayList<>();

        for (Map.Entry<Long, Agg> entry : sortedEntries) {
            Long recipeId = entry.getKey();
            Agg agg = entry.getValue();

            String recipeName = agg.getRecipeTitle();

            // 가족 알레르기 기준으로 unsafe면 가족 위시리스트 결과에서 제외
            boolean usableForMealPlan = isUsableForMealPlan(
                    familyRoomId,
                    agg.getIngredientsRaw(),
                    familyAllergens
            );
            if (!usableForMealPlan) {
                continue;
            }

            result.add(new FamilyWishListItemResDTO(
                    recipeId,
                    recipeName,
                    "https://cdn.example.com/foods/" + recipeId + "/thumbnail.jpg",
                    4.5,
                    new FamilyWishListItemResDTO.FoodCategory("TEMP", "임시 카테고리"),
                    true,
                    new FamilyWishListItemResDTO.SourceProfile(new ArrayList<>(agg.getProfiles().values()))
            ));
        }

        return result;
    }

    /**
     * 방장만 가족 위시리스트 항목 삭제 (=exclusion 등록)
     * - 개인이 해당 항목을 삭제했다가 다시 담으면 exclusion 해제 (로직은 개인 위시리스트 add 시점에서 수행)
     */
    @Transactional
    public void deleteFamilyWishListItems(Long memberId, Long familyRoomId, List<Long> recipeId) {

        // 방장 검증
        familyRoomService.validateLeader(memberId, familyRoomId);

        if (recipeId == null || recipeId.isEmpty()) {
            return;
        }

        familyWishListExclusionRepository.excludeRecipes(familyRoomId, recipeId);
    }

    /**
     * usableForMealPlan 판단
     * - AllergySubstitutionService의 matchedAllergens 로직 재사용
     */
    private boolean isUsableForMealPlan(Long familyRoomId, String ingredientsRaw, List<Allergen> familyAllergens) {
        // 가족 알레르기가 없으면 모두 안전
        if (familyAllergens == null || familyAllergens.isEmpty()) {
            return true;
        }

        // 재료 정보가 없으면 false
        if (ingredientsRaw == null || ingredientsRaw.isBlank()) {
            return false;
        }

        Map<Allergen, ?> matched = allergySubstitutionService.generateSubstitutionRules(
                familyRoomId,
                splitIngredientsRaw(ingredientsRaw)
        );

        return matched == null || matched.isEmpty();
    }

    private String normalize(String s) {
        if (s == null) return "";

        // 소문자화
        String lowered = s.toLowerCase(Locale.ROOT);

        // 공백 포함, 대부분의 구분기호/특수문자를 제거
        return lowered.replaceAll("[^0-9a-zA-Z가-힣]", "");
    }

    private List<String> splitIngredientsRaw(String ingredientsRaw) {
        if (ingredientsRaw == null || ingredientsRaw.isBlank()) {
            return List.of();
        }

        String[] parts = ingredientsRaw.split("[\\n\\r,;/\\t]+|[·•]");

        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (!t.isEmpty()) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    /**
     * recipeId별 집계용 내부 클래스
     * - distinct profiles (인원 수)
     * - latest wish id (최신)
     * - recipeTitle, ingredientsRaw, normalizedIngredientsBlob(usableForMealPlan 계산용)
     */
    private class Agg {
        private final String recipeTitle;
        private final String ingredientsRaw;
        private final String normalizedIngredientsBlob;
        private final Map<Long, FamilyWishListItemResDTO.Profile> profiles = new LinkedHashMap<>();
        private long latestWishId = 0L;

        private Agg(Recipe recipe) {
            this.recipeTitle = (recipe == null) ? null : recipe.getTitle();
            this.ingredientsRaw = (recipe == null) ? null : recipe.getIngredientsRaw();
            this.normalizedIngredientsBlob = normalize(this.ingredientsRaw);
        }

        private void add(MemberWishList w) {
            if (w == null) return;

            if (w.getId() != null) {
                latestWishId = Math.max(latestWishId, w.getId());
            }

            FamilyMemberProfile p = w.getFamilyMemberProfile();
            if (p == null || p.getId() == null) return;

            profiles.putIfAbsent(
                    p.getId(),
                    new FamilyWishListItemResDTO.Profile(p.getId(), p.getNickname())
            );
        }

        private int getWisherCount() {
            return profiles.size();
        }

        private long getLatestWishId() {
            return latestWishId;
        }

        private String getRecipeTitle() {
            return recipeTitle;
        }

        private Map<Long, FamilyWishListItemResDTO.Profile> getProfiles() {
            return profiles;
        }

        private String getIngredientsRaw() {
            return ingredientsRaw;
        }

        private String getNormalizedIngredientsBlob() {
            return normalizedIngredientsBlob;
        }
    }
}
