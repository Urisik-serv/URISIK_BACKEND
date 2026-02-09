package com.urisik.backend.domain.recipe.repository;

import com.urisik.backend.domain.recipe.entity.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findBySourceRef(String sourceRef);

    List<Recipe> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    /** Lightweight candidate row for meal plan generation (avoid loading full entity graph) */
    interface RecipeCandidateRow {
        Long getId();
        String getIngredientsRaw();
    }

    /** Fetch a limited random-ish slice of recipes for candidate building. */
    @Query("""
        select r.id as id, r.ingredientsRaw as ingredientsRaw
        from Recipe r
        order by function('rand')
    """)
    List<RecipeCandidateRow> findRandomCandidateRows(Pageable pageable);



    @Query("""
        select r
        from Recipe r
        where r.ingredientsRaw like concat('%', :keyword, '%')
          and (:excludeIds is null or r.id not in :excludeIds)
        order by function('rand')
    """)
    List<Recipe> findByIngredientLikeExcludeIdsRandom(
            @Param("keyword") String keyword,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable
    );

}