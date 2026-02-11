package com.urisik.backend.domain.search.dto;

import com.urisik.backend.domain.search.enums.RankChange;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PopularKeywordResponse {

    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private List<KeywordItem> keywords;

    @Getter
    @AllArgsConstructor
    public static class KeywordItem {
        private String keyword;
        private int rank;
        private RankChange change;
    }
}

