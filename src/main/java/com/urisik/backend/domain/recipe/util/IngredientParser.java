package com.urisik.backend.domain.recipe.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class IngredientParser {

    // 단위/수량 제거용 (필요하면 계속 확장)
    private static final Pattern QUANTITY_PATTERN = Pattern.compile(
            "\\b\\d+(\\.\\d+)?\\b|\\([^)]*\\)|" +                 // 숫자, 괄호
                    "(g|kg|ml|l|컵|큰술|작은술|T|t|스푼|개|장|줌|약간|적당량|조금|쪽)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 식약처 RCP_PARTS_DTLS 같은 원문 재료 문자열을 List<String>으로 변환
     * - 구분자: 콤마, 줄바꿈, ·
     * - 수량/단위 제거
     */
    public List<String> parse(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        // 흔한 접두어 제거
        String normalized = raw
                .replace("재료", "")
                .replace("재료정보", "")
                .replace(":", " ");

        // 구분자 통일
        String[] tokens = normalized.split("[,\\n\\r·]+");

        List<String> out = new ArrayList<>();
        for (String t : tokens) {
            String s = t.trim();
            if (s.isBlank()) continue;

            // 수량/단위 제거
            s = QUANTITY_PATTERN.matcher(s).replaceAll(" ");
            s = s.replaceAll("\\s+", " ").trim();

            // 너무 짧은 토큰 제거(옵션)
            if (s.isBlank()) continue;

            out.add(s);
        }

        // 중복 제거는 지금은 하지 않음(원문 보존 목적). 필요 시 distinct 가능.
        return out;
    }
}
