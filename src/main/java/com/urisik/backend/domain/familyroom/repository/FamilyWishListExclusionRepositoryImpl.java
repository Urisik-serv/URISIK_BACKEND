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
        batchInsertExclusions(familyRoomId, recipeIds, "recipe_id");
    }

    @Override
    @Transactional
    public void excludeTransformedRecipes(Long familyRoomId, List<Long> transformedRecipeIds) {
        if (transformedRecipeIds == null || transformedRecipeIds.isEmpty()) {
            return;
        }
        batchInsertExclusions(familyRoomId, transformedRecipeIds, "transformed_recipe_id");
    }

    private void batchInsertExclusions(Long familyRoomId, List<Long> ids, String columnName) {

        Set<Long> uniqueIdSet = new HashSet<>(ids);
        List<Long> uniqueIds = uniqueIdSet.stream()
                .filter(Objects::nonNull)
                .toList();

        if (uniqueIds.isEmpty()) {
            return;
        }

        final int CHUNK_SIZE = 500;

        for (int start = 0; start < uniqueIds.size(); start += CHUNK_SIZE) {
            List<Long> chunk = uniqueIds.subList(
                    start,
                    Math.min(start + CHUNK_SIZE, uniqueIds.size())
            );

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO family_wishlist_exclusion (family_room_id, ")
              .append(columnName)
              .append(", create_at, updated_at) VALUES ");

            List<Object> params = new ArrayList<>();

            for (int i = 0; i < chunk.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("(?, ?, NOW(), NOW())");
                params.add(familyRoomId);
                params.add(chunk.get(i));
            }

            sb.append(" ON DUPLICATE KEY UPDATE ")
              .append(columnName)
              .append(" = ")
              .append(columnName)
              .append(", updated_at = NOW()");

            var q = em.createNativeQuery(sb.toString());
            for (int i = 0; i < params.size(); i++) {
                q.setParameter(i + 1, params.get(i));
            }
            q.executeUpdate();
        }
    }
}
