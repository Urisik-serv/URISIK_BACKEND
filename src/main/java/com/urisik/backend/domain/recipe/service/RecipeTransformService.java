package com.urisik.backend.domain.recipe.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.urisik.backend.domain.allergy.entity.AllergenAlternative;
import com.urisik.backend.domain.allergy.enums.Allergen;
import com.urisik.backend.domain.allergy.repository.AllergenAlternativeRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.recipe.converter.TransformedRecipeConverter;
import com.urisik.backend.domain.recipe.dto.req.TransformRecipeRequestDTO;
import com.urisik.backend.domain.recipe.dto.res.TransformedRecipeResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.TransformedRecipe;
import com.urisik.backend.domain.recipe.enums.RecipeErrorCode;
import com.urisik.backend.domain.recipe.enums.Visibility;
import com.urisik.backend.domain.recipe.infrastructure.ai.AiTransformedRecipePayload;
import com.urisik.backend.domain.recipe.infrastructure.ai.RecipeStepNormalizer;
import com.urisik.backend.domain.recipe.infrastructure.ai.RecipeStepSerializer;
import com.urisik.backend.domain.recipe.infrastructure.ai.RecipeTransformPromptBuilder;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.TransformedRecipeRepository;
import com.urisik.backend.global.ai.AiClient;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;





