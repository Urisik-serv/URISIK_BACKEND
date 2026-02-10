package com.urisik.backend.domain.searchLog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private LocalDateTime updatedAt;

    public PopularKeyword(String keyword, long count, int rank) {
        this.keyword = keyword;
        this.count = count;
        this.rank = rank;
        this.updatedAt = LocalDateTime.now();
    }
}

