package com.urisik.backend.domain.searchLog.controller;

import com.urisik.backend.domain.searchLog.batch.PopularKeywordBatch;
import com.urisik.backend.domain.searchLog.entity.PopularKeyword;
import com.urisik.backend.domain.searchLog.enums.SearchLogSuccessCode;
import com.urisik.backend.domain.searchLog.repository.PopularKeywordRepository;
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
@Tag(name = "searchLog", description = "인기 검색어 관련 API")
public class PopularKeywordController {

    private final PopularKeywordRepository popularKeywordRepository;
    private final PopularKeywordBatch popularKeywordBatch;

    @GetMapping("/popular")
    @Operation(summary = "인기검색어 API", description = "사용자 검색 데이터를 기반으로 사람들이 많이 검색한 키워드를 Top 8를 추천하는 api 입니다.")
    public ApiResponse<List<String>> popularKeywords() {

        List<String> keywords =
                popularKeywordRepository.findAllByOrderByRankAsc()
                        .stream()
                        .map(PopularKeyword::getKeyword)
                        .toList();

        return ApiResponse.onSuccess(
                SearchLogSuccessCode.POPULAR_KEYWORD_FETCHED,keywords
        );
    }

    @PostMapping("/admin/search/popular/batch")
    @Operation(summary = "인기 검색어 api 수동 실행용 API" ,description = "인기 검색어 api 사용하기 전 실행해주새요")
    public void runPopularBatch() {
        popularKeywordBatch.aggregate();
    }

}

