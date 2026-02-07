package com.urisik.backend.domain.recipe.init;

import com.urisik.backend.domain.recipe.dto.req.ExternalRecipeSnapshotDTO;
import org.springframework.stereotype.Component;
import com.urisik.backend.domain.recipe.dto.req.ExternalRecipeUpsertRequestDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExternalRecipeAssembler {

    public ExternalRecipeUpsertRequestDTO assemble(ExternalRecipeSnapshotDTO s) {

        // 1️. instructionsRaw 생성
        String instructionsRaw = s.getSteps().stream()
                .map(step -> step.getOrder() + ". " + step.getDescription())
                .collect(Collectors.joining("\n"));

        // 2. Step 변환
        List<ExternalRecipeUpsertRequestDTO.Step> steps =
                s.getSteps().stream()
                        .map(st -> new ExternalRecipeUpsertRequestDTO.Step(
                                st.getOrder(),
                                st.getDescription(),
                                st.getImageUrl()
                        ))
                        .toList();

        // 3️. Metadata 생성
        ExternalRecipeUpsertRequestDTO.Metadata metadata =
                new ExternalRecipeUpsertRequestDTO.Metadata(
                        trimToNull(s.getCategory()),
                        normalizeServingWeight(s.getServingWeight()),
                        safeInt(s.getCalorie()),
                        safeInt(s.getCarbohydrate()),
                        safeInt(s.getProtein()),
                        safeInt(s.getFat()),
                        safeInt(s.getSodium()),
                        trimToNull(s.getImageSmall()),
                        trimToNull(s.getImageLarge())
                );

        return new ExternalRecipeUpsertRequestDTO(
                s.getRcpSeq(),
                s.getRcpNm(),
                s.getIngredientsRaw(),
                instructionsRaw,
                metadata,
                steps
        );
    }

    /* ===== util ===== */

    private Integer safeInt(String s) {
        try {
            return (s == null || s.isBlank()) ? null : (int) Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private String normalizeServingWeight(String w) {
        return "1인분";
    }
}

