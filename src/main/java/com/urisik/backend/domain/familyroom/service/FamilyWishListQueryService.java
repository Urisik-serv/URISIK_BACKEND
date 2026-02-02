package com.urisik.backend.domain.familyroom.service;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.exception.FamilyRoomException;
import com.urisik.backend.domain.familyroom.exception.code.FamilyRoomErrorCode;
import com.urisik.backend.domain.familyroom.repository.FamilyWishListExclusionRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.member.entity.MemberTransformedRecipeWish;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.member.repo.MemberWishListRepository;
import com.urisik.backend.domain.member.repo.MemberTransformedRecipeWishRepository;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyWishListQueryService {

    private final MemberWishListRepository memberWishListRepository;
    private final MemberTransformedRecipeWishRepository memberTransformedRecipeWishRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;
    private final FamilyRoomService familyRoomService;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;

    // 가족방 전체 알레르기 집계
    private final MemberAllergyRepository memberAllergyRepository;

    /**
     * 가족 위시리스트 조회
     * - 개인 위시리스트(MemberWishList)를 가족방 단위로 집계
     * - 방장이 삭제한 항목은 exclusion에 의해 조회에서만 제외
     * - 가족 알레르기 필터링:
     *   레시피 재료(ingredientsRaw) 기준으로 가족방 전체 알레르기(Allergen) 포함 여부를 판별
     *   unsafe(알레르기 포함) 레시피는 가족 위시리스트 결과에서 제외
     */
    public List<FamilyWishListItemResDTO> getFamilyWishList(Long memberId, Long familyRoomId) {

        // 가족방 구성원만 가능
        validateFamilyRoomMember(memberId, familyRoomId);

        // 방장 제외 목록 조회 (exclusion)
        Set<Long> excludedRecipeIds =
                familyWishListExclusionRepository.findExcludedRecipeIdsByFamilyRoomId(familyRoomId);
        Set<Long> excludedTransformedRecipeIds =
                familyWishListExclusionRepository.findExcludedTransformedRecipeIdsByFamilyRoomId(familyRoomId);

        // 가족방 전체 알레르기 조회
        List<Allergen> familyAllergens =
                memberAllergyRepository.findDistinctAllergensByFamilyRoomId(familyRoomId);

        // 알레르겐 정규화
        List<String> normalizedAllergens = familyAllergens.stream()
                .filter(Objects::nonNull)
                .map(Allergen::getKoreanName)
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        // 가족방 내 전체 개인 위시리스트 조회 (recipe + profile join fetch)
        List<MemberWishList> all = memberWishListRepository.findAllByFamilyRoomIdWithRecipe(familyRoomId);
        List<MemberTransformedRecipeWish> allTrans = memberTransformedRecipeWishRepository.findAllByFamilyRoomIdWithRecipe(familyRoomId);
        boolean emptyCanonical = (all == null || all.isEmpty());
        boolean emptyTrans = (allTrans == null || allTrans.isEmpty());
        if (emptyCanonical && emptyTrans) return List.of();

        // exclusion 반영 + recipeId 기준 집계 (canonical + transformed)
        Map<WishKey, Agg> grouped = new LinkedHashMap<>();

        // canonical wishes
        for (MemberWishList w : (all == null ? List.<MemberWishList>of() : all)) {
            if (w == null || w.getRecipe() == null || w.getFamilyMemberProfile() == null) continue;

            Long recipeId = w.getRecipe().getId();
            if (recipeId == null) continue;

            if (excludedRecipeIds != null && excludedRecipeIds.contains(recipeId)) continue;

            WishKey key = WishKey.canonical(recipeId);
            Agg agg = grouped.computeIfAbsent(key, k -> Agg.fromRecipe(w.getRecipe()));
            agg.addCanonical(w);
        }

        // transformed wishes
        for (MemberTransformedRecipeWish w : (allTrans == null ? List.<MemberTransformedRecipeWish>of() : allTrans)) {
            if (w == null || w.getRecipe() == null || w.getFamilyMemberProfile() == null) continue;

            Long transId = w.getRecipe().getId();
            if (transId == null) continue;

            if (excludedTransformedRecipeIds != null && excludedTransformedRecipeIds.contains(transId)) continue;

            WishKey key = WishKey.transformed(transId);
            Agg agg = grouped.computeIfAbsent(key, k -> Agg.fromTransformed(w.getRecipe()));
            agg.addTransformed(w);
        }

        if (grouped.isEmpty()) return List.of();

        // 정렬
        // - 개인 위시리스트에 담은 인원 수 많은 순 (DESC)
        // - 최신순 (latest wish id DESC)
        List<Map.Entry<WishKey, Agg>> sortedEntries = new ArrayList<>(grouped.entrySet());

        sortedEntries.sort((e1, e2) -> {
            int cmp = Integer.compare(e2.getValue().getWisherCount(), e1.getValue().getWisherCount());
            if (cmp != 0) return cmp;
            return Long.compare(e2.getValue().getLatestWishId(), e1.getValue().getLatestWishId());
        });

        // DTO 변환
        List<FamilyWishListItemResDTO> result = new ArrayList<>();

        for (Map.Entry<WishKey, Agg> entry : sortedEntries) {
            WishKey key = entry.getKey();
            Agg agg = entry.getValue();

            String recipeName = agg.getTitle();

            // 가족 알레르기 기준으로 unsafe면 가족 위시리스트 결과에서 제외
            boolean usableForMealPlan = isUsableForMealPlan(
                    agg.getIngredientsRaw(),
                    normalizedAllergens
            );
            if (!usableForMealPlan) continue;

            Long recipeId = (key.type() == WishKey.Type.CANONICAL) ? key.id() : null;
            Long transformedRecipeId = (key.type() == WishKey.Type.TRANSFORMED) ? key.id() : null;

            result.add(new FamilyWishListItemResDTO(
                    recipeId,
                    transformedRecipeId,
                    recipeName,
                    "https://cdn.example.com/foods/" + key.id() + "/thumbnail.jpg",
                    4.5,
                    new FamilyWishListItemResDTO.FoodCategory("TEMP", "임시 카테고리"),
                    usableForMealPlan,
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
    public void deleteFamilyWishListItems(
            Long memberId,
            Long familyRoomId,
            List<Long> recipeId,
            List<Long> transformedRecipeId
    ) {

        // 가족방 구성원만 가능
        validateFamilyRoomMember(memberId, familyRoomId);

        // 방장 검증
        familyRoomService.validateLeader(memberId, familyRoomId);

        boolean hasCanonical = recipeId != null && !recipeId.isEmpty();
        boolean hasTransformed = transformedRecipeId != null && !transformedRecipeId.isEmpty();

        if (!hasCanonical && !hasTransformed) {
            return;
        }

        if (hasCanonical) {
            familyWishListExclusionRepository.excludeRecipes(familyRoomId, recipeId);
        }

        if (hasTransformed) {
            familyWishListExclusionRepository.excludeTransformedRecipes(familyRoomId, transformedRecipeId);
        }
    }

    private void validateFamilyRoomMember(Long memberId, Long familyRoomId) {
        familyMemberProfileRepository.findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.NOT_FAMILY_MEMBER));
    }

    /**
     * usableForMealPlan 판단
     * - getFamilyWishList에서 미리 조회한 가족 알레르기를 사용해 in-memory로 매칭
     */
    private boolean isUsableForMealPlan(String ingredientsRaw, List<String> normalizedAllergens) {
        if (normalizedAllergens == null || normalizedAllergens.isEmpty()) {
            return true;
        }

        if (ingredientsRaw == null || ingredientsRaw.isBlank()) {
            return false;
        }

        List<String> normalizedIngredients = splitIngredientsRaw(ingredientsRaw).stream()
                .map(this::normalize)
                .filter(s -> s != null && !s.isBlank())
                .toList();

        if (normalizedIngredients.isEmpty()) {
            return false;
        }

        for (String allergenKey : normalizedAllergens) {
            if (allergenKey == null || allergenKey.isBlank()) continue;

            boolean matched = normalizedIngredients.stream()
                    .anyMatch(ing -> ing.contains(allergenKey));

            if (matched) return false;
        }

        return true;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String lowered = s.toLowerCase(Locale.ROOT);
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
            if (!t.isEmpty()) tokens.add(t);
        }
        return tokens;
    }

    // Typed key to distinguish canonical and transformed wishes
    private record WishKey(Type type, Long id) {
        enum Type { CANONICAL, TRANSFORMED }

        static WishKey canonical(Long id) {
            return new WishKey(Type.CANONICAL, id);
        }

        static WishKey transformed(Long id) {
            return new WishKey(Type.TRANSFORMED, id);
        }
    }

    /**
     * recipeId별 집계용 내부 클래스
     * - distinct profiles (인원 수)
     * - latest wish id (최신)
     * - title, ingredientsRaw (알레르기 필터링용)
     */
    private static class Agg {
        private final String title;
        private final String ingredientsRaw;
        private final Map<Long, FamilyWishListItemResDTO.Profile> profiles = new LinkedHashMap<>();
        private long latestWishId = 0L;

        private Agg(String title, String ingredientsRaw) {
            this.title = title;
            this.ingredientsRaw = ingredientsRaw;
        }

        private static Agg fromRecipe(Recipe recipe) {
            String t = (recipe == null) ? null : recipe.getTitle();
            String ing = (recipe == null) ? null : recipe.getIngredientsRaw();
            return new Agg(t, ing);
        }

        private static Agg fromTransformed(TransformedRecipe recipe) {
            String t = (recipe == null) ? null : recipe.getTitle();
            String ing = (recipe == null) ? null : recipe.getIngredientsTransformed();
            return new Agg(t, ing);
        }

        private void addCanonical(MemberWishList w) {
            if (w == null) return;
            if (w.getId() != null) {
                latestWishId = Math.max(latestWishId, w.getId());
            }
            addProfile(w.getFamilyMemberProfile());
        }

        private void addTransformed(MemberTransformedRecipeWish w) {
            if (w == null) return;
            if (w.getId() != null) {
                latestWishId = Math.max(latestWishId, w.getId());
            }
            addProfile(w.getFamilyMemberProfile());
        }

        private void addProfile(FamilyMemberProfile p) {
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

        private String getTitle() {
            return title;
        }

        private Map<Long, FamilyWishListItemResDTO.Profile> getProfiles() {
            return profiles;
        }

        private String getIngredientsRaw() {
            return ingredientsRaw;
        }
    }
}
