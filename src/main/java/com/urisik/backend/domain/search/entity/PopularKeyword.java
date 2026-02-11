package com.urisik.backend.domain.search.entity;

import com.urisik.backend.domain.search.enums.RankChange;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularKeyword {

    @Id
    private String keyword;

    private long count;

    @Column(name = "rank_order")
    private int rank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RankChange change;

    @Column(nullable = false)
    private LocalDateTime windowStart;

    @Column(nullable = false)
    private LocalDateTime windowEnd;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PopularKeyword(
            String keyword,
            long count,
            int rank,
            RankChange change,
            LocalDateTime windowStart,
            LocalDateTime windowEnd
    ) {
        this.keyword = keyword;
        this.count = count;
        this.rank = rank;
        this.change = change;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.updatedAt = LocalDateTime.now();
    }
}


