package com.kume.kume.application.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.Rating;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.RecipeMediaType;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.presentation.mappers.RecipeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RatingService ratingService;
    private final RecipeRepository recipeRepository;
    private final RecipeMediaService recipeMediaService;
    private final RecipeMapper recipeMapper;

    @Transactional
    public Result<RecipeResponse> createRecipe(CreateRecipeRequest request) throws IOException {

        Recipe recipe = recipeMapper.toEntity(request);
        Recipe savedRecipe = recipeRepository.save(recipe);

        MultipartFile cover = request.getImageFile();
        if (cover != null && !cover.isEmpty()) {
            String filename = recipeMediaService.saveMedia(cover);
            savedRecipe.setImageUrl(filename);

        }

        if (request.getExtraImages() != null) {
            for (MultipartFile file : request.getExtraImages()) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                String filename = recipeMediaService.saveMedia(file);
                RecipeMediaType type = resolveMediaType(file.getContentType());

                RecipeMediaDTO dto = RecipeMediaDTO.builder()
                        .recipeId(savedRecipe.getId())
                        .mediaUrl(filename)
                        .mediaType(type)
                        .build();

                recipeMediaService.create(dto);
            }
        }

        return Result.success(
                "Receta creada exitosamente",
                recipeMapper.toResponse(savedRecipe)
        );
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getAllRecipes() {
        List<RecipeResponse> recipes = recipeRepository.findAll().stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());
        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<RecipeResponse> getRecipeById(Long id) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(id);

        if (recipeOpt.isEmpty()) {
            return Result.failure("Recipe not found");
        }

        Recipe recipe = recipeOpt.get();
        RecipeResponse dto = recipeMapper.toResponse(recipe);

        double avg = ratingService.getAverageRating(recipe.getId());
        dto.setAverageRating(avg);

        Map<Integer, Long> counts = ratingService.getRatingCounts(recipe.getId());
        dto.setRatingCounts(counts);

        Long loggedUserId = 1L;
        Optional<Rating> userRatingOpt = ratingService.getUserRating(loggedUserId, recipe.getId());
        dto.setUserRating(userRatingOpt.map(Rating::getStars).orElse(null));

        return Result.success("OK", dto);
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipeByName(String name) {
        List<RecipeResponse> recipes = recipeRepository.findByName(name)
                .stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());
        return Result.success("Receta encontrada exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipesByDifficulty(String difficulty) {
        List<RecipeResponse> recipes = recipeRepository.findByDifficulty(difficulty.toUpperCase())
                .stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());
        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional
    public Result<RecipeResponse> updateRecipe(Long id, UpdateRecipeRequest request) {
        Optional<Recipe> existingRecipe = recipeRepository.findById(id);

        if (!existingRecipe.isPresent()) {
            return Result.failure("Receta no encontrada");
        }

        Recipe updatedRecipe = recipeMapper.updateEntity(existingRecipe.get(), request);
        Recipe savedRecipe = recipeRepository.save(updatedRecipe);

        return Result.success("Receta actualizada exitosamente", recipeMapper.toResponse(savedRecipe));
    }

    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> searchRecipes(String query, String type, String country,
            DifficultyLevel difficulty) {

        final String lowerCaseQuery = (query != null) ? query.toLowerCase() : "";

        List<RecipeResponse> recipes = recipeRepository
                .findAllWithDetails()
                .stream()
                .map(recipeMapper::toResponse)
                .filter(recipe -> lowerCaseQuery.isEmpty()
                || recipe.getName().toLowerCase().contains(lowerCaseQuery)
                || recipe.getIngredients().stream().anyMatch(
                        ri -> ri.getIngredient().getName().toLowerCase().contains(lowerCaseQuery)))
                .filter(recipe -> type == null || type.isEmpty() || type.equalsIgnoreCase(recipe.getType()))
                .filter(recipe -> country == null || country.isEmpty() || country.equalsIgnoreCase(recipe.getCountry()))
                .filter(recipe -> difficulty == null || difficulty == recipe.getDifficulty())
                .collect(Collectors.toList());

        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    private RecipeMediaType resolveMediaType(String contentType) {
        if (contentType == null) {
            return RecipeMediaType.IMAGE;
        }
        if (contentType.startsWith("image/")) {
            return RecipeMediaType.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return RecipeMediaType.VIDEO;
        }
        return RecipeMediaType.IMAGE;
    }
}
