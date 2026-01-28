package com.urisik.backend.domain.review.dto;

import lombok.Builder;

@Builder
public record ReviewRequestDto (
        Integer score,
        Boolean isFavorite

){}
