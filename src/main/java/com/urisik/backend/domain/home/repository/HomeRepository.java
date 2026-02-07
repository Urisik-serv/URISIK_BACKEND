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

    /**
     * 전체 고평점 레시피
     * - 리뷰 수 → 평점 → 위시 수
     */
    @Query("""
    select r
    from Recipe r
    join r.recipeExternalMetadata rem
    order by
        r.avgScore desc,
        r.reviewCount desc,
        r.wishCount desc
""")
    List<Recipe> findTopByScore(Pageable pageable);

    /**
     * 카테고리별 고평점 레시피 조회
     */
    @Query("""
    select r
    from Recipe r
    join r.recipeExternalMetadata rem
    where rem.category in :categories
    order by
        r.avgScore desc,
        r.reviewCount desc,
        r.wishCount desc
""")
    List<Recipe> findTopByCategories(
            @Param("categories") List<String> categories,
            Pageable pageable
    );


}
