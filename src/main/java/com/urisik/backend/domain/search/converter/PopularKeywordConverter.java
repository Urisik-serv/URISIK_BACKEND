package com.urisik.backend.domain.search.converter;

import com.urisik.backend.domain.search.dto.PopularKeywordResponse;
import com.urisik.backend.domain.search.entity.PopularKeyword;

import java.time.LocalDateTime;
import java.util.List;

public final class PopularKeywordConverter {

    private PopularKeywordConverter() {}

    public static PopularKeywordResponse toResponse(
            List<PopularKeyword> list
    ) {

        if (list.isEmpty()) {
            return new PopularKeywordResponse(null, null, List.of());
        }

        LocalDateTime windowStart = list.get(0).getWindowStart();
        LocalDateTime windowEnd = list.get(0).getWindowEnd();

        List<PopularKeywordResponse.KeywordItem> items =
                list.stream()
                        .map(pk -> new PopularKeywordResponse.KeywordItem(
                                pk.getKeyword(),
                                pk.getRank(),
                                pk.getRankChange()
                        ))
                        .toList();

        return new PopularKeywordResponse(
                windowStart,
                windowEnd,
                items
        );
    }
}

