package com.urisik.backend.domain.familyroom.repository;


import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FamilyWishListExclusionRepositoryImpl implements FamilyWishListExclusionRepositoryCustom {

    private final EntityManager em;

    @Override
    @Transactional
    public void excludeRecipes(Long familyRoomId, List<Long> recipeIds) {

        if (recipeIds == null || recipeIds.isEmpty()) {
            return;
        }

        // 요청 자체의 중복 제거 (같은 recipeId가 여러 번 들어오면 중복 insert 시도 위험)
        Set<Long> uniqueRecipeIdSet = new HashSet<>(recipeIds);
        List<Long> uniqueRecipeIds = uniqueRecipeIdSet.stream().filter(Objects::nonNull).toList();

        if (uniqueRecipeIds.isEmpty()) {
            return;
        }

        // 중복은 INSERT IGNORE로 무시
        // 큰 IN/VALUES는 패킷/파라미터 제한에 걸릴 수 있으므로 chunking
        final int CHUNK_SIZE = 500;

        for (int start = 0; start < uniqueRecipeIds.size(); start += CHUNK_SIZE) {
            List<Long> chunk = uniqueRecipeIds.subList(start, Math.min(start + CHUNK_SIZE, uniqueRecipeIds.size()));

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT IGNORE INTO family_wish_list_exclusion (family_room_id, recipe_id) VALUES ");

            List<Object> params = new ArrayList<>();
            for (int i = 0; i < chunk.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("(?, ?)");
                params.add(familyRoomId);
                params.add(chunk.get(i));
            }

            var q = em.createNativeQuery(sb.toString());
            for (int i = 0; i < params.size(); i++) {
                q.setParameter(i + 1, params.get(i));
            }
            q.executeUpdate();
        }
    }

    @Override
    @Transactional
    public void excludeTransformedRecipes(Long familyRoomId, List<Long> transformedRecipeIds) {

        if (transformedRecipeIds == null || transformedRecipeIds.isEmpty()) {
            return;
        }

        // 요청 자체의 중복 제거 (같은 transformedRecipeId가 여러 번 들어오면 중복 insert 시도 위험)
        Set<Long> uniqueIdSet = new HashSet<>(transformedRecipeIds);
        List<Long> uniqueIds = uniqueIdSet.stream().filter(Objects::nonNull).toList();

        if (uniqueIds.isEmpty()) {
            return;
        }

        // 중복은 INSERT IGNORE로 무시
        final int CHUNK_SIZE = 500;

        for (int start = 0; start < uniqueIds.size(); start += CHUNK_SIZE) {
            List<Long> chunk = uniqueIds.subList(start, Math.min(start + CHUNK_SIZE, uniqueIds.size()));

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT IGNORE INTO family_wish_list_exclusion (family_room_id, transformed_recipe_id) VALUES ");

            List<Object> params = new ArrayList<>();
            for (int i = 0; i < chunk.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("(?, ?)");
                params.add(familyRoomId);
                params.add(chunk.get(i));
            }

            var q = em.createNativeQuery(sb.toString());
            for (int i = 0; i < params.size(); i++) {
                q.setParameter(i + 1, params.get(i));
            }
            q.executeUpdate();
        }
    }
}
