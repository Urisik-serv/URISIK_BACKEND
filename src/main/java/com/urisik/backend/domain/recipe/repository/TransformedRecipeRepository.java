package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransformedRecipeRepository extends JpaRepository<TransformedRecipe, Long> {

    @Query("""
    select tr
    from TransformedRecipe tr
    join fetch tr.baseRecipe r
    where r.title like %:keyword%
""")
    List<TransformedRecipe> findByRecipeTitleLike(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<TransformedRecipe> findByFamilyRoomId(Long familyRoomId);

    @Query("""
        select tr
        from TransformedRecipe tr
        join fetch tr.baseRecipe br
        where tr.familyRoomId = :familyRoomId
          and tr.id in :ids
    """)
    List<TransformedRecipe> findAllByFamilyRoomIdAndIdIn(
            @Param("familyRoomId") Long familyRoomId,
            @Param("ids") Collection<Long> ids
    );

    List<TransformedRecipe> findByFamilyRoomIdAndBaseRecipe_IdIn(
            Long familyRoomId,
            Collection<Long> recipeIds
    );
}
