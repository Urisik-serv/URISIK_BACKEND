package com.urisik.backend.domain.searchLog.service;

import com.urisik.backend.domain.searchLog.entity.SearchLog;
import com.urisik.backend.domain.searchLog.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    @Async
    @Transactional
    public void logSearch(Long userId, String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return;
        }

        searchLogRepository.save(
                SearchLog.of(userId, keyword)
        );
    }
}
