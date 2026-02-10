package com.urisik.backend.domain.searchLog.batch;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SearchLogCleanupBatch {

    private final EntityManager em;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void cleanup() {

        em.createQuery("""
            delete from SearchLog s
            where s.searchedAt < :threshold
        """)
                .setParameter(
                        "threshold",
                        LocalDateTime.now().minusDays(30)
                )
                .executeUpdate();
    }
}

