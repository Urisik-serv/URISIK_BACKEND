package com.urisik.backend.domain.search.repository;

import com.urisik.backend.domain.search.entity.SearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query("""
    select
        s.normalizedKeyword,
        count(s),
        max(s.searchedAt)
    from SearchLog s
    where s.searchedAt between :start and :end
    group by s.normalizedKeyword
    order by count(s) desc, max(s.searchedAt) desc
""")
    List<Object[]> findTopKeywords(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}

