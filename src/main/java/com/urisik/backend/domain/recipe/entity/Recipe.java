package com.urisik.backend.domain.recipe.entity;

import com.urisik.backend.domain.member.entity.MemberWishList;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 음식명
    @Column(nullable = false)
    private String name;

    // 별점
    @Column(nullable = false)
    private Double avgScore = 0.0;

    // 해당 레시피에 대해 작성된 전체 리뷰 개수
    @Column(nullable = false)
    private Long reviewCount = 0L;

    // 검증된 재료 (중립 데이터)
    @ElementCollection
    @CollectionTable(name = "recipe_ingredient", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient", nullable = false)
    private List<String> ingredients;


    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MemberWishList> memberWishLists = new ArrayList<>();


    public Recipe(String name, List<String> ingredients) {
        this.name = name;
        this.ingredients = ingredients;
    }

    public void updateAvgScore (Integer newScore) {
        double totalScore = this.avgScore * this.reviewCount;
        this.reviewCount++;
        this.avgScore = (totalScore + newScore) / this.reviewCount;
        this.avgScore = Math.round(this.avgScore * 10) / 10.0;
    }

}
