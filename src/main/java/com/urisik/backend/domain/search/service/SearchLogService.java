package com.urisik.backend.domain.search.service;

import com.urisik.backend.domain.search.entity.SearchLog;
import com.urisik.backend.domain.search.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    @Async
    @Transactional
    public void logSearch(Long userId, String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return;
        }

        try {
            searchLogRepository.save(
                    SearchLog.of(userId, keyword)
            );
        } catch (Exception e) {
            // 로그 저장 실패해도 검색에는 영향 없어야 함
            log.warn("Search log save failed: {}", e.getMessage());
        }
    }
}
