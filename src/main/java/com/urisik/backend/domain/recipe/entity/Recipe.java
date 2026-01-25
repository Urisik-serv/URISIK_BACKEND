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

}
