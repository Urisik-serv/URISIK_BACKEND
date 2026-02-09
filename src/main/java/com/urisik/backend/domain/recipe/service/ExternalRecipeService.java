package com.urisik.backend.domain.recipe.service;

import com.urisik.backend.domain.recipe.converter.ExternalRecipeConverter;
import com.urisik.backend.domain.recipe.dto.ExternalRecipeSnapshotDTO;
import com.urisik.backend.domain.recipe.dto.req.ExternalRecipeUpsertRequestDTO;
import com.urisik.backend.domain.recipe.dto.res.ExternalRecipeUpsertResponseDTO;
import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.entity.RecipeExternalMetadata;
import com.urisik.backend.domain.recipe.entity.RecipeStep;
import com.urisik.backend.domain.recipe.init.ExternalRecipeAssembler;
import com.urisik.backend.domain.recipe.repository.RecipeExternalMetadataRepository;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import com.urisik.backend.domain.recipe.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExternalRecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeExternalMetadataRepository metadataRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final ExternalRecipeConverter converter;
    private final ExternalRecipeAssembler assembler;

    @Transactional
    public ExternalRecipeUpsertResponseDTO upsertFromSnapshot(
            ExternalRecipeSnapshotDTO snapshot
    ) {
        // 이미 있으면 반환
        Optional<Recipe> existing =
                recipeRepository.findBySourceRef(snapshot.getRcpSeq());
        if (existing.isPresent()) {
            return new ExternalRecipeUpsertResponseDTO(existing.get().getId(), false);
        }

        ExternalRecipeUpsertRequestDTO command =
                assembler.assemble(snapshot);

        Recipe recipe = converter.toRecipe(command);
        Recipe saved = recipeRepository.save(recipe);

        RecipeExternalMetadata meta =
                converter.toMetadata(saved, command);
        metadataRepository.save(meta);

        if (command.getSteps() != null && !command.getSteps().isEmpty()) {
            List<RecipeStep> steps = command.getSteps().stream()
                    .map(s -> new RecipeStep(
                            saved,
                            s.getOrder(),
                            s.getDescription(),
                            s.getImageUrl()
                    ))
                    .toList();

            recipeStepRepository.saveAll(steps);
        }

        return new ExternalRecipeUpsertResponseDTO(saved.getId(), true);
    }
}





