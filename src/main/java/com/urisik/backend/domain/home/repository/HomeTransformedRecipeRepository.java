package com.urisik.backend.domain.home.repository;

import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HomeTransformedRecipeRepository
        extends JpaRepository<TransformedRecipe, Long> {

    @Query("""
        select tr
        from TransformedRecipe tr
        join fetch tr.baseRecipe br
        left join fetch br.recipeExternalMetadata
        order by tr.wishCount desc
    """)
    List<TransformedRecipe> findTopForHome(Pageable pageable);
}
