package com.urisik.backend.domain.recipe.entity;

import com.urisik.backend.domain.member.entity.MemberWishList;
import com.urisik.backend.domain.recipe.enums.SourceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipe")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // 외부 API 원본 재료 문자열 그대로
    @Lob
    @Column(nullable = false)
    private String ingredientsRaw;

    // 외부 API 단계들을 합친 원본 조리법 문자열 그대로(줄바꿈 포함)
    @Lob
    @Column(nullable = false)
    private String instructionsRaw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    // 외부 API RCP_SEQ 또는 AI 요청 ID 같은 참조값
    private String sourceRef;

    public Recipe(
            String title,
            String ingredientsRaw,
            String instructionsRaw,
            SourceType sourceType,
            String sourceRef
    ) {
        this.title = title;
        this.ingredientsRaw = ingredientsRaw;
        this.instructionsRaw = instructionsRaw;
        this.sourceType = sourceType;
        this.sourceRef = sourceRef;
    }

}

