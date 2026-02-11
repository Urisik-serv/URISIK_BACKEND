package com.urisik.backend.domain.search.controller;

import com.urisik.backend.domain.search.batch.PopularKeywordBatch;
import com.urisik.backend.domain.search.dto.PopularKeywordResponse;
import com.urisik.backend.domain.search.entity.PopularKeyword;
import com.urisik.backend.domain.search.enums.SearchLogSuccessCode;
import com.urisik.backend.domain.search.repository.PopularKeywordRepository;
import com.urisik.backend.domain.search.service.PopularKeywordService;
import com.urisik.backend.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
@Tag(name = "Search", description = "인기 검색어 관련 API")
public class PopularKeywordController {

    private final PopularKeywordService popularKeywordService;
    private final PopularKeywordBatch popularKeywordBatch;

    @GetMapping("/popular")
    @Operation(summary = "인기 검색어 조회 API", description = "사용자 검색 데이터를 기반으로 최근 24시간 기준 인기 검색어 Top 8을 조회하는 api 입니다."
    )
    public ApiResponse<PopularKeywordResponse> popularKeywords() {

        return ApiResponse.onSuccess(
                SearchLogSuccessCode.POPULAR_KEYWORD_FETCHED,
                popularKeywordService.getPopularKeywords()
        );
    }

    @PostMapping("/admin/popular/batch")
    @Operation(summary = "인기 검색어 배치 수동 실행 API (개발용)", description = "개발/테스트 환경에서 인기 검색어 집계를 즉시 실행하는 api 입니다.")
    public ApiResponse<String> runPopularBatch() {

        popularKeywordBatch.aggregate();

        return ApiResponse.onSuccess(
                SearchLogSuccessCode.POPULAR_KEYWORD_BATCH_EXECUTED,
                "Batch executed successfully"
        );
    }
}


