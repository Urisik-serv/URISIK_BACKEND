package com.urisik.backend.domain.searchLog.repository;

import com.urisik.backend.domain.searchLog.entity.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopularKeywordRepository
        extends JpaRepository<PopularKeyword, String> {

    List<PopularKeyword> findAllByOrderByRankAsc();
}

