package com.urisik.backend.domain.familyroom.repository;


import com.urisik.backend.domain.familyroom.entity.FamilyRoom;
import com.urisik.backend.domain.familyroom.entity.FamilyWishListExclusion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FamilyWishListExclusionRepositoryImpl implements FamilyWishListExclusionRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    @Transactional
    public void excludeRecipes(Long familyRoomId, List<Long> recipeIds) {

        if (recipeIds == null || recipeIds.isEmpty()) {
            return;
        }

        // 이미 exclusion에 있는 recipeId들 조회 (중복 삽입 방지)
        List<Long> existing = em.createQuery("""
                select e.recipeId
                from FamilyWishListExclusion e
                where e.familyRoom.id = :familyRoomId
                  and e.recipeId in :recipeIds
            """, Long.class)
                .setParameter("familyRoomId", familyRoomId)
                .setParameter("recipeIds", recipeIds)
                .getResultList();

        Set<Long> existingSet = new HashSet<>(existing);

        // 없는 것만 insert
        FamilyRoom familyRoomRef = em.getReference(FamilyRoom.class, familyRoomId);

        for (Long recipeId : recipeIds) {
            if (existingSet.contains(recipeId)) continue;

            em.persist(FamilyWishListExclusion.of(familyRoomRef, recipeId));
        }
    }
}
