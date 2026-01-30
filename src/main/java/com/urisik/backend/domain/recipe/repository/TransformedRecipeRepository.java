package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.Visibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TransformedRecipeRepository extends JpaRepository<TransformedRecipe, Long> {

    @Query("""
        select tr
        from TransformedRecipe tr
        join fetch tr.recipe r
        where tr.visibility = :visibility
          and r.title like %:keyword%
    """)
    List<TransformedRecipe> findPublicByRecipeTitleLike(String keyword, Visibility visibility, Pageable pageable);

    Optional<TransformedRecipe> findByRecipe_IdAndFamilyRoomId(Long recipeId, Long familyRoomId);
    List<TransformedRecipe> findByFamilyRoomId(Long familyRoomId);
    List<TransformedRecipe> findByVisibility(Visibility visibility);

}
