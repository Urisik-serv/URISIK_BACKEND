package com.urisik.backend.domain.allergy.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeAllergyCheckRequestDTO {

    private List<String> ingredients;

}
