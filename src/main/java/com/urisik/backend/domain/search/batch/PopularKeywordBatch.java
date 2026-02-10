package com.urisik.backend.domain.search.batch;

import com.urisik.backend.domain.search.entity.PopularKeyword;
import com.urisik.backend.domain.search.repository.PopularKeywordRepository;
import com.urisik.backend.domain.search.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PopularKeywordBatch {

    private final SearchLogRepository searchLogRepository;
    private final PopularKeywordRepository popularKeywordRepository;

    @Scheduled(cron = "0 0 */3 * * *")
    @Transactional
    public void aggregate() {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(24);

        List<Object[]> results =
                searchLogRepository.findTopKeywords(
                        start,
                        end,
                        PageRequest.of(0, 8)
                );

        popularKeywordRepository.deleteAll();

        int rank = 1;
        for (Object[] row : results) {
            popularKeywordRepository.save(
                    new PopularKeyword(
                            (String) row[0],
                            (Long) row[1],
                            rank++
                    )
            );
        }
    }
}

