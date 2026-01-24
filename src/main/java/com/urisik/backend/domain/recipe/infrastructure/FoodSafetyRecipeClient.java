package com.urisik.backend.domain.recipe.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FoodSafetyRecipeClient {

    private final WebClient foodSafetyWebClient;
    private final FoodSafetyProperties props;
    private final ObjectMapper objectMapper;

    /**
     * 음식명으로 검색 (부분검색)
     * - 식약처 API는 startIdx/endIdx 페이징이 필요함
     * - MVP에선 1~N 범위로 호출 (limit 기반)
     */
    public List<ExternalRecipeRaw> searchByName(String name, int startIdx, int endIdx) {
        try {
            String encoded = URLEncoder.encode(name, StandardCharsets.UTF_8);

            // /{apiKey}/{serviceId}/{dataType}/{startIdx}/{endIdx}?RCP_NM=...
            String path = String.format(
                    "/%s/%s/%s/%d/%d?RCP_NM=%s",
                    props.getApiKey(),
                    props.getServiceId(),
                    props.getDataType(),
                    startIdx,
                    endIdx,
                    encoded
            );

            String body = foodSafetyWebClient.get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractRows(body);

        } catch (Exception e) {
            throw new GeneralException(RecipeErrorCode.RECIPE_EXTERNAL_API_ERROR, e.getMessage());
        }
    }

    /**
     * recipeKey(EXT-xxxx) 상세 조회용
     *
     * ⚠️ 식약처가 RCP_SEQ 단건 조회 파라미터를 공식 제공하지 않는 경우가 있어
     * MVP에선 넉넉한 범위에서 row를 읽고 RCP_SEQ로 필터링하는 방식으로 구현.
     * (나중에 캐싱/인덱싱/DB 저장으로 최적화 권장)
     */
    public ExternalRecipeRaw findByRcpSeq(String rcpSeq) {
        // MVP: 1~1000 범위를 200개씩 훑어보기 (프로젝트 상황에 맞게 조정)
        int pageSize = 200;
        int maxEnd = 1000;

        for (int start = 1; start <= maxEnd; start += pageSize) {
            int end = Math.min(start + pageSize - 1, maxEnd);

            List<ExternalRecipeRaw> page = searchByName("", start, end); // 빈 검색으로 범위 조회 시도
            for (ExternalRecipeRaw r : page) {
                if (rcpSeq.equals(r.getRCP_SEQ())) return r;
            }
        }
        return null;
    }

    private List<ExternalRecipeRaw> extractRows(String json) throws Exception {
        if (json == null || json.isBlank()) return List.of();

        JsonNode root = objectMapper.readTree(json);
        JsonNode cook = root.get(props.getServiceId());
        if (cook == null) return List.of();

        JsonNode row = cook.get("row");
        if (row == null || !row.isArray()) return List.of();

        return objectMapper.convertValue(row, new TypeReference<List<ExternalRecipeRaw>>() {});
    }

}

