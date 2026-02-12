package com.urisik.backend.domain.search.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "search_log",
        indexes = {
                @Index(name = "idx_search_log_keyword", columnList = "normalizedKeyword"),
                @Index(name = "idx_search_log_time", columnList = "searchedAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @Column(nullable = false)
    private String normalizedKeyword;

    private Long userId;

    @Column(nullable = false)
    private LocalDateTime searchedAt;

    private SearchLog(String keyword, String normalizedKeyword, Long userId) {
        this.keyword = keyword;
        this.normalizedKeyword = normalizedKeyword;
        this.userId = userId;
        this.searchedAt = LocalDateTime.now();
    }

    public static SearchLog of(Long userId, String keyword) {
        return new SearchLog(keyword, normalize(keyword), userId);
    }

    private static String normalize(String keyword) {
        return keyword
                .toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9가-힣]", "");
    }
}

