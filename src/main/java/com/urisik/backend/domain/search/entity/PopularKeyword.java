package com.urisik.backend.domain.search.entity;

import com.urisik.backend.domain.search.enums.RankChange;
import com.urisik.backend.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   //

    @Column(nullable = false)
    private String keyword;

    private long count;

    @Column(name = "rank_order")
    private int rank;

    @Enumerated(EnumType.STRING)
    @Column(name = "rank_change", length = 20)
    private RankChange rankChange;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

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
        this.rankChange = change;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }
}


