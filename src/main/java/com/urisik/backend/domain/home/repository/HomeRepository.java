package com.urisik.backend.domain.home.repository;

import com.urisik.backend.domain.recipe.entity.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HomeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
        select r
        from Recipe r
        left join fetch r.recipeExternalMetadata
        order by r.wishCount desc
    """)
    List<Recipe> findTopForHome(Pageable pageable);

}
