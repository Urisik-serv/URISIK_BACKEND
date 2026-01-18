package com.urisik.backend.allergy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.allergy.controller.RecipeAllergyController;
import com.urisik.backend.domain.allergy.dto.req.RecipeAllergyCheckRequestDTO;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.enums.AllergySuccessCode;
import com.urisik.backend.domain.allergy.service.AllergySubstitutionService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RecipeAllergyController.class)
class RecipeAllergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllergySubstitutionService allergySubstitutionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("레시피에 알레르기 식품이 포함된 경우 대체 식재료를 반환한다")
    @WithMockUser(username = "1")
    void recipeAllergyCheck_success() throws Exception {
        // given
        Long loginUserId = 1L;

        RecipeAllergyCheckRequestDTO request =
                new RecipeAllergyCheckRequestDTO(List.of(
                        "고구마 100g(2/3개)",
                        "설탕 2g",
                        "잣 8g(8알)"
                ));

        Map<Allergen, List<String>> serviceResult = Map.of(
                Allergen.PINE_NUT,
                List.of("참깨", "해바라기씨")
        );

        Mockito.when(
                allergySubstitutionService.checkAndMapSubstitutions(
                        Mockito.eq(loginUserId),
                        Mockito.anyList()
                )
        ).thenReturn(serviceResult);

        // when & then
        mockMvc.perform(post("/api/recipes/allergy-check")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> loginUserId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code")
                        .value(AllergySuccessCode.RECIPE_ALLERGY_CHECK_OK.getCode()))
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andExpect(jsonPath("$.result[0].allergen").value("잣"))
                .andExpect(jsonPath("$.result[0].alternatives", hasSize(2)))
                .andExpect(jsonPath("$.result[0].alternatives",
                        containsInAnyOrder("참깨", "해바라기씨")));
    }

    @Test
    @DisplayName("레시피에 알레르기 식품이 없는 경우 빈 리스트를 반환한다")
    @WithMockUser(username = "1")
    void recipeAllergyCheck_noAllergy() throws Exception {
        // given
        Long loginUserId = 1L;

        RecipeAllergyCheckRequestDTO request =
                new RecipeAllergyCheckRequestDTO(List.of(
                        "고구마 100g",
                        "설탕 2g"
                ));

        Mockito.when(
                allergySubstitutionService.checkAndMapSubstitutions(
                        Mockito.eq(loginUserId),
                        Mockito.anyList()
                )
        ).thenReturn(Map.of());

        // when & then
        mockMvc.perform(post("/api/recipes/allergy-check")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> loginUserId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result", hasSize(0)));
    }

}
