package com.urisik.backend.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodRecommendOption {

    EXCLUDE("완전 제외"),
    ALLOW_ALTERNATIVE("대체 허용");

    private final String koreanDescription;
}
