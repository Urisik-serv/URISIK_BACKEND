package com.urisik.backend.domain.home.repository;

import com.urisik.backend.domain.recipe.entity.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HomeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
        select r
        from Recipe r
        left join fetch r.recipeExternalMetadata
        order by r.wishCount desc
    """)
    List<Recipe> findTopForHome(Pageable pageable);

    @Query("""
    select r
    from Recipe r
    join fetch r.recipeExternalMetadata m
    order by r.avgScore desc
""")
    List<Recipe> findTopByScore(Pageable pageable);

    @Query("""
    select r
    from Recipe r
    join fetch r.recipeExternalMetadata m
    where m.category in :categories
    order by r.avgScore desc
""")
    List<Recipe> findTopByCategories(
            @Param("categories") List<String> categories,
            Pageable pageable
    );

}
