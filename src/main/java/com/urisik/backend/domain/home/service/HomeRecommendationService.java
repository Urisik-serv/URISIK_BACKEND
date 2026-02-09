package com.urisik.backend.domain.home.service;

import com.urisik.backend.domain.home.candidate.HomeRecipeCandidate;
import com.urisik.backend.domain.home.candidate.RecipeCandidate;
import com.urisik.backend.domain.home.candidate.TransformedRecipeCandidate;
import com.urisik.backend.domain.home.repository.HomeRepository;
import com.urisik.backend.domain.home.repository.HomeTransformedRecipeRepository;
import com.urisik.backend.domain.member.entity.FamilyMemberProfile;
import com.urisik.backend.domain.member.repo.FamilyMemberProfileRepository;
import com.urisik.backend.domain.home.converter.HomeSafeRecipeConverter;
import com.urisik.backend.domain.recipe.converter.RecipeTextParser;
import com.urisik.backend.domain.home.dto.HomeSafeRecipeDTO;
import com.urisik.backend.domain.home.dto.HomeSafeRecipeResponse;
import com.urisik.backend.domain.recipe.service.AllergyRiskService;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeRecommendationService {

    private final HomeRepository homeRepository;
    private final HomeTransformedRecipeRepository transformedRecipeRepository;
    private final FamilyMemberProfileRepository familyMemberProfileRepository;
    private final AllergyRiskService allergyRiskService;
    private final HomeSafeRecipeConverter converter;

    @Transactional(readOnly = true)
    public HomeSafeRecipeResponse recommendSafeRecipes(Long loginUserId) {

        FamilyMemberProfile profile =
                familyMemberProfileRepository.findByMember_Id(loginUserId)
                        .orElseThrow(() ->
                                new GeneralException(GeneralErrorCode.NOT_FOUND));

        Long familyRoomId = profile.getFamilyRoom().getId();

        List<HomeRecipeCandidate> candidates = new ArrayList<>();

        candidates.addAll(
                homeRepository.findTopForHome(PageRequest.of(0, 20))
                        .stream()
                        .map(RecipeCandidate::new)
                        .collect(Collectors.toList())
        );

        candidates.addAll(
                transformedRecipeRepository.findTopForHome(PageRequest.of(0, 20))
                        .stream()
                        .map(TransformedRecipeCandidate::new)
                        .collect(Collectors.toList())
        );


        candidates.sort(
                Comparator.comparingInt(HomeRecipeCandidate::getWishCount)
                        .reversed()
        );

        List<HomeSafeRecipeDTO> result = new ArrayList<>();

        for (HomeRecipeCandidate candidate : candidates) {

            List<String> ingredients = candidate.getIngredients();

            boolean hasRisk =
                    !allergyRiskService
                            .detectRiskAllergens(familyRoomId, ingredients)
                            .isEmpty();

            if (!hasRisk) {
                result.add(converter.toDto(candidate));
            }

            if (result.size() == 3) break;
        }

        return new HomeSafeRecipeResponse(result);
    }
}
