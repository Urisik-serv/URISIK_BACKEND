package com.urisik.backend.domain.familyroom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;

import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.MemberAllergyRepository;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO.Category;
import com.urisik.backend.domain.familyroom.dto.res.FamilyWishListItemResDTO.Profile;
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
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.service.AllergyRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyWishListQueryService {

    private static final ObjectMapper CURSOR_MAPPER = new ObjectMapper();
    private static final long CURSOR_TTL_SECONDS = 60L * 60L * 24L * 7L; // 7 days

    private final FamilyRoomService familyRoomService;
    private final AllergyRiskService allergyRiskService;
    private final MemberWishListRepository memberWishListRepository;
    private final MemberTransformedRecipeWishRepository memberTransformedRecipeWishRepository;
    private final FamilyWishListExclusionRepository familyWishListExclusionRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
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
        return getFamilyWishList(memberId, familyRoomId, null, 20).items();
    }

    public PageResult getFamilyWishList(
            Long memberId,
            Long familyRoomId,
            Cursor cursor,
            int size
    ) {

        // size 방어
        int pageSize = Math.max(1, Math.min(size, 50));

        // 가족방 구성원만 가능
        validateFamilyRoomMember(memberId, familyRoomId);

        // 방장 제외 목록 조회 (exclusion)
        Set<Long> excludedRecipeIds =
                familyWishListExclusionRepository.findExcludedRecipeIdsByFamilyRoomId(familyRoomId);
        Set<Long> excludedTransformedRecipeIds =
                familyWishListExclusionRepository.findExcludedTransformedRecipeIdsByFamilyRoomId(familyRoomId);

        // 가족방 내 전체 개인 위시리스트 조회 (recipe + profile join fetch)
        List<MemberWishList> all = memberWishListRepository.findAllByFamilyRoomIdWithRecipe(familyRoomId);
        List<MemberTransformedRecipeWish> allTrans = memberTransformedRecipeWishRepository.findAllByFamilyRoomIdWithRecipe(familyRoomId);
        boolean emptyCanonical = (all == null || all.isEmpty());
        boolean emptyTrans = (allTrans == null || allTrans.isEmpty());
        if (emptyCanonical && emptyTrans) return PageResult.empty();

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

            Long transformedRecipeId = w.getRecipe().getId();
            if (transformedRecipeId == null) continue;

            if (excludedTransformedRecipeIds != null && excludedTransformedRecipeIds.contains(transformedRecipeId)) continue;

            WishKey key = WishKey.transformed(transformedRecipeId);
            Agg agg = grouped.computeIfAbsent(key, k -> Agg.fromTransformed(w.getRecipe()));
            agg.addTransformed(w);
        }

        if (grouped.isEmpty()) return PageResult.empty();

        /**
         * 정렬
         * - 담은 인원 수 많은 순 (DESC)
         * - 평점 높은 순 (avgScore DESC)
         * - 최신순 (latest wish id DESC)
         * - tie-breaker: type(CANONICAL 먼저) -> id DESC
         */
        List<Map.Entry<WishKey, Agg>> sortedEntries = new ArrayList<>(grouped.entrySet());
        sortedEntries.sort(this::compareEntryRank);

        // 알레르기 필터를 먼저 적용 (페이지 빈칸 방지)
        List<Allergen> familyAllergens =
                memberAllergyRepository.findDistinctAllergensByFamilyRoomId(familyRoomId);

        List<Map.Entry<WishKey, Agg>> usableEntries = new ArrayList<>(sortedEntries.size());
        for (Map.Entry<WishKey, Agg> entry : sortedEntries) {
            Agg agg = entry.getValue();

            List<String> ingredients = splitIngredientsRaw(agg.getIngredientsRaw());
            boolean safe = allergyRiskService.detectRiskAllergens(familyAllergens, ingredients).isEmpty();
            agg.setAllergySafe(safe);

            if (safe) {
                usableEntries.add(entry);
            }
        }

        if (usableEntries.isEmpty()) return PageResult.empty();

        // 커서 적용: cursor 이후 항목부터
        int startIdx = 0;
        if (cursor != null && cursor.isValid()) {
            startIdx = usableEntries.size(); // 기본: 결과 없음
            for (int i = 0; i < usableEntries.size(); i++) {
                Map.Entry<WishKey, Agg> e = usableEntries.get(i);
                // e 가 cursor 이후면(compareEntryToCursor > 0) 시작점
                if (compareEntryToCursor(e, cursor) > 0) {
                    startIdx = i;
                    break;
                }
            }
        }

        if (startIdx >= usableEntries.size()) {
            return PageResult.empty();
        }

        // page slice (+1개를 더 가져와 hasNext 판단)
        int endExclusive = Math.min(usableEntries.size(), startIdx + pageSize + 1);
        List<Map.Entry<WishKey, Agg>> window = usableEntries.subList(startIdx, endExclusive);

        boolean hasNext = window.size() > pageSize;
        List<Map.Entry<WishKey, Agg>> pageEntries = hasNext ? window.subList(0, pageSize) : window;

        // DTO 변환
        List<FamilyWishListItemResDTO> items = new ArrayList<>(pageEntries.size());
        for (Map.Entry<WishKey, Agg> entry : pageEntries) {
            WishKey key = entry.getKey();
            Agg agg = entry.getValue();

            String type = toApiType(key.type());
            Long id = key.id();
            String title = agg.getTitle();
            String allergyStatus = agg.isAllergySafe() ? "SAFE" : "DANGEROUS";

            items.add(new FamilyWishListItemResDTO(
                    type,
                    id,
                    title,
                    agg.getImageUrl(),
                    agg.getAvgScore(),
                    allergyStatus,
                    agg.getCategory(),
                    agg.getIngredientsRaw(),
                    new FamilyWishListItemResDTO.SourceProfile(new ArrayList<>(agg.getProfiles().values()))
            ));
        }

        Cursor nextCursor = null;
        if (hasNext && !pageEntries.isEmpty()) {
            Map.Entry<WishKey, Agg> last = pageEntries.get(pageEntries.size() - 1);
            nextCursor = Cursor.from(last.getKey(), last.getValue());
        }

        return new PageResult(items, hasNext, nextCursor);
    }

    private int compareEntryRank(Map.Entry<WishKey, Agg> e1, Map.Entry<WishKey, Agg> e2) {
        Agg a1 = e1.getValue();
        Agg a2 = e2.getValue();

        // wisherCount DESC
        int cmp = Integer.compare(a2.getWisherCount(), a1.getWisherCount());
        if (cmp != 0) return cmp;

        // avgScore DESC
        cmp = Double.compare(scoreValue(a2.getAvgScore()), scoreValue(a1.getAvgScore()));
        if (cmp != 0) return cmp;

        // latestWishId DESC
        cmp = Long.compare(a2.getLatestWishId(), a1.getLatestWishId());
        if (cmp != 0) return cmp;

        // type: CANONICAL 먼저
        cmp = Integer.compare(typeOrder(e1.getKey().type()), typeOrder(e2.getKey().type()));
        if (cmp != 0) return cmp;

        // id DESC
        return Long.compare(
                e2.getKey().id() == null ? 0L : e2.getKey().id(),
                e1.getKey().id() == null ? 0L : e1.getKey().id()
        );
    }

    private int compareEntryToCursor(Map.Entry<WishKey, Agg> entry, Cursor cursor) {
        if (cursor == null) return 1;

        Agg a = entry.getValue();
        WishKey k = entry.getKey();

        // wisherCount DESC
        int cmp = Integer.compare(cursor.wisherCount, a.getWisherCount());
        if (cmp != 0) return cmp;

        // avgScore DESC
        cmp = Double.compare(cursor.avgScore, scoreValue(a.getAvgScore()));
        if (cmp != 0) return cmp;

        // latestWishId DESC
        cmp = Long.compare(cursor.latestWishId, a.getLatestWishId());
        if (cmp != 0) return cmp;

        // type: RECIPE 먼저
        cmp = Integer.compare(typeOrder(k.type()), apiTypeOrder(cursor.type));
        if (cmp != 0) return cmp;

        // id DESC
        long cursorId = cursor.id == null ? 0L : cursor.id;
        long entryId = k.id() == null ? 0L : k.id();
        return Long.compare(cursorId, entryId);
    }

    private int apiTypeOrder(String type) {
        if (type == null) return 0;
        String t = type.trim().toUpperCase(Locale.ROOT);
        return "RECIPE".equals(t) ? 0 : 1; // RECIPE first
    }

    private static double scoreValue(Double avgScore) {
        return (avgScore == null) ? -1.0d : avgScore;
    }

    private int typeOrder(WishKey.Type type) {
        // CANONICAL 먼저
        return (type == WishKey.Type.CANONICAL) ? 0 : 1;
    }

    private static String toApiType(WishKey.Type type) {
        if (type == null) return "RECIPE";
        return (type == WishKey.Type.CANONICAL) ? "RECIPE" : "TRANSFORMED_RECIPE";
    }

    public record PageResult(
            List<FamilyWishListItemResDTO> items,
            boolean hasNext,
            Cursor nextCursor
    ) {
        static PageResult empty() {
            return new PageResult(List.of(), false, null);
        }
    }

    public static class Cursor {
        private final int wisherCount;
        private final long latestWishId;
        private final double avgScore;
        private final String type;
        private final Long id;

        public Cursor(int wisherCount, long latestWishId, double avgScore, String type, Long id) {
            this.wisherCount = wisherCount;
            this.latestWishId = latestWishId;
            this.avgScore = avgScore;
            this.type = type;
            this.id = id;
        }

        public int getWisherCount() {
            return wisherCount;
        }

        public long getLatestWishId() {
            return latestWishId;
        }

        public double getAvgScore() {
            return avgScore;
        }

        public String getType() {
            return type;
        }

        public Long getId() {
            return id;
        }

        public boolean isValid() {
            return wisherCount >= 0
                    && latestWishId >= 0
                    && avgScore >= -1.0d
                    && type != null && !type.isBlank()
                    && id != null && id > 0;
        }

        static Cursor from(WishKey key, Agg agg) {
            return new Cursor(
                    agg == null ? 0 : agg.getWisherCount(),
                    agg == null ? 0L : agg.getLatestWishId(),
                    agg == null ? -1.0d : scoreValue(agg.getAvgScore()),
                    key == null ? "RECIPE" : toApiType(key.type()),
                    key == null ? null : key.id()
            );
        }

        public static Cursor of(Integer wisherCount, Long latestWishId, Double avgScore, String type, Long id) {
            if (wisherCount == null || latestWishId == null || avgScore == null || type == null || id == null) return null;
            String t = type.trim().toUpperCase(Locale.ROOT);
            if (!"RECIPE".equals(t) && !"TRANSFORMED_RECIPE".equals(t)) {
                return null;
            }
            return new Cursor(wisherCount, latestWishId, avgScore, t, id);
        }

        public String encode() {
            try {
                long exp = Instant.now().getEpochSecond() + CURSOR_TTL_SECONDS;

                ObjectNode node = CURSOR_MAPPER.createObjectNode();
                node.put("w", wisherCount);
                node.put("l", latestWishId);
                node.put("s", avgScore);
                String tShort = "RECIPE".equalsIgnoreCase(type) ? "R"
                        : ("TRANSFORMED_RECIPE".equalsIgnoreCase(type) ? "T" : null);
                node.put("t", tShort);
                node.put("id", id);
                node.put("exp", exp);

                byte[] jsonBytes = CURSOR_MAPPER.writeValueAsBytes(node);
                return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(jsonBytes);
            } catch (Exception e) {
                // encode 실패 시 커서 미제공
                return null;
            }
        }

        public static Cursor decode(String token) {
            if (token == null || token.isBlank()) return null;

            try {
                byte[] decoded = java.util.Base64.getUrlDecoder().decode(token);
                JsonNode node = CURSOR_MAPPER.readTree(decoded);

                int w = node.path("w").asInt(-1);
                long l = node.path("l").asLong(-1L);
                double s = node.path("s").asDouble(-1.0d);
                String tRaw = node.path("t").asText(null);
                long id = node.path("id").asLong(-1L);
                long exp = node.path("exp").asLong(-1L);

                if (w < 0 || l < 0 || s < -1.0d || id < 0 || tRaw == null || exp < 0) return null;

                long now = Instant.now().getEpochSecond();
                if (exp < now) return null; // expired

                String type;
                if ("R".equalsIgnoreCase(tRaw)) type = "RECIPE";
                else if ("T".equalsIgnoreCase(tRaw)) type = "TRANSFORMED_RECIPE";
                else return null;

                Cursor cursor = new Cursor(w, l, s, type, id);
                return cursor.isValid() ? cursor : null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 방장만 가족 위시리스트 항목 삭제 (=exclusion 등록)
     * - 개인이 해당 항목을 삭제했다가 다시 담으면 exclusion 해제 (로직은 개인 위시리스트 add 시점에서 수행)
     */
    @Transactional
    public void deleteFamilyWishListItems(
            Long memberId,
            Long familyRoomId,
            List<WishItemKey> items
    ) {
        // 가족방 구성원만 가능
        validateFamilyRoomMember(memberId, familyRoomId);

        // 방장 검증
        familyRoomService.validateLeader(memberId, familyRoomId);

        if (items == null || items.isEmpty()) {
            return;
        }

        List<Long> recipeIds = new ArrayList<>();
        List<Long> transformedIds = new ArrayList<>();

        for (WishItemKey item : items) {
            if (item == null || item.id() == null || item.type() == null) continue;
            String t = item.type().trim().toUpperCase(Locale.ROOT);
            if ("RECIPE".equals(t)) {
                recipeIds.add(item.id());
            } else if ("TRANSFORMED_RECIPE".equals(t)) {
                transformedIds.add(item.id());
            }
        }

        boolean hasCanonical = !recipeIds.isEmpty();
        boolean hasTransformed = !transformedIds.isEmpty();

        if (!hasCanonical && !hasTransformed) {
            return;
        }

        if (hasCanonical) {
            Set<Long> existing = memberWishListRepository.findExistingRecipeIds(familyRoomId, recipeIds);
            Set<Long> uniqueRecipeIds = new HashSet<>(recipeIds);
            if (existing.size() != uniqueRecipeIds.size()) {
                throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_WISHLIST_NOT_FOUND);
            }
            familyWishListExclusionRepository.excludeRecipes(familyRoomId, new ArrayList<>(uniqueRecipeIds));
        }

        if (hasTransformed) {
            Set<Long> existing = memberTransformedRecipeWishRepository.findExistingTransformedRecipeIds(familyRoomId, transformedIds);
            Set<Long> uniqueTransformedIds = new HashSet<>(transformedIds);
            if (existing.size() != uniqueTransformedIds.size()) {
                throw new FamilyRoomException(FamilyRoomErrorCode.FAMILY_WISHLIST_NOT_FOUND);
            }
            familyWishListExclusionRepository.excludeTransformedRecipes(familyRoomId, new ArrayList<>(uniqueTransformedIds));
        }
    }

    // Unified key for deleteFamilyWishListItems
    public record WishItemKey(String type, Long id) {}

    private void validateFamilyRoomMember(Long memberId, Long familyRoomId) {
        familyMemberProfileRepository.findByFamilyRoom_IdAndMember_Id(familyRoomId, memberId)
                .orElseThrow(() -> new FamilyRoomException(FamilyRoomErrorCode.NOT_FAMILY_MEMBER));
    }

    private List<String> splitIngredientsRaw(String ingredientsRaw) {
        if (ingredientsRaw == null || ingredientsRaw.isBlank()) {
            return List.of();
        }

        String[] parts = ingredientsRaw.split("[\\n\\r,;/\\t·•]+");

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

    /** recipeId별 집계용 내부 클래스 */
    private static class Agg {
        private final String title;
        private final String ingredientsRaw;
        private final String imageUrl;
        private final Double avgScore;
        private final Category category;

        private boolean allergySafe = false; // computed per request
        private final Map<Long, Profile> profiles = new LinkedHashMap<>();
        private long latestWishId = 0L;

        private Agg(String title,
                    String ingredientsRaw,
                    String imageUrl,
                    Double avgScore,
                    Category category) {
            this.title = title;
            this.ingredientsRaw = ingredientsRaw;
            this.imageUrl = imageUrl;
            this.avgScore = avgScore;
            this.category = category;
        }

        private static Agg fromRecipe(Recipe recipe) {
            String t = (recipe == null) ? null : recipe.getTitle();
            String ing = (recipe == null) ? null : recipe.getIngredientsRaw();

            RecipeExternalMetadata meta = (recipe == null) ? null : recipe.getRecipeExternalMetadata();

            String imageUrl = (meta == null) ? null : meta.getImageSmallUrl();
            Double avgScore = (recipe == null) ? null : recipe.getAvgScore();

            Category category = null;
            if (meta != null && meta.getCategory() != null) {
                category = new Category(
                        meta.getCategory(),
                        meta.getCategory()
                );
            }

            return new Agg(t, ing, imageUrl, avgScore, category);
        }

        private static Agg fromTransformed(TransformedRecipe recipe) {
            String t = (recipe == null) ? null : recipe.getTitle();
            String ing = (recipe == null) ? null : recipe.getIngredientsRaw();

            String imageUrl = null;
            if (recipe != null && recipe.getBaseRecipe() != null) {
                RecipeExternalMetadata meta = recipe.getBaseRecipe().getRecipeExternalMetadata();
                if (meta != null) {
                    imageUrl = meta.getThumbnailImageUrl();
                }
            }

            Double avgScore = (recipe == null) ? null : recipe.getAvgScore();

            Category category = null;
            if (recipe != null && recipe.getBaseRecipe() != null) {
                RecipeExternalMetadata meta = recipe.getBaseRecipe().getRecipeExternalMetadata();
                if (meta != null && meta.getCategory() != null) {
                    category = new Category(
                            meta.getCategory(),
                            meta.getCategory()
                    );
                }
            }

            return new Agg(t, ing, imageUrl, avgScore, category);
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
                    new Profile(p.getId(), p.getNickname(), p.getProfilePicUrl())
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

        private Map<Long, Profile> getProfiles() {
            return profiles;
        }

        private String getIngredientsRaw() {
            return ingredientsRaw;
        }

        private String getImageUrl() {
            return imageUrl;
        }

        private Double getAvgScore() {
            return avgScore;
        }

        private Category getCategory() {
            return category;
        }

        private boolean isAllergySafe() {
            return allergySafe;
        }

        private void setAllergySafe(boolean allergySafe) {
            this.allergySafe = allergySafe;
        }
    }
}
