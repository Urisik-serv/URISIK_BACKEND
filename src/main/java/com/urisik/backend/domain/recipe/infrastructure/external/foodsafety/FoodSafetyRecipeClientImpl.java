package com.urisik.backend.domain.recipe.infrastructure.external.foodsafety;

import com.urisik.backend.domain.recipe.infrastructure.external.foodsafety.dto.FoodSafetyRecipeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FoodSafetyRecipeClientImpl implements FoodSafetyRecipeClient {

    private final RestTemplate restTemplate;

    @Value("${foodsafety.api.key}")
    private String apiKey;

    @Value("${foodsafety.api.serviceId:COOKRCP01}")
    private String serviceId;

    private static final String BASE = "http://openapi.foodsafetykorea.go.kr/api";

    @Override
    public FoodSafetyRecipeResponse.Row fetchOneByRcpSeq(String rcpSeq) {
        String url = String.format("%s/%s/%s/json/%d/%d/RCP_SEQ=%s",
                BASE, apiKey, serviceId, 1, 1, rcpSeq);

        ResponseEntity<FoodSafetyRecipeResponse> res =
                restTemplate.exchange(url, HttpMethod.GET, null, FoodSafetyRecipeResponse.class);

        FoodSafetyRecipeResponse body = res.getBody();
        if (body == null || body.getCookrcp01() == null || body.getCookrcp01().getRow() == null) {
            return null;
        }
        List<FoodSafetyRecipeResponse.Row> rows = body.getCookrcp01().getRow();
        return rows.isEmpty() ? null : rows.get(0);
    }
}
