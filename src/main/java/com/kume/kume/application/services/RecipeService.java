package com.kume.kume.application.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.Rating;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.presentation.mappers.RecipeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RatingService ratingService;
    // private final RecipeMapper recipeMapper;
    @Autowired
    private final RecipeRepository recipeRepository;

    @Transactional
    public Result<RecipeResponse> createRecipe(CreateRecipeRequest request) {
        Recipe recipe = RecipeMapper.toEntity(request);
        Recipe savedRecipe = recipeRepository.save(recipe);
        
        return Result.success("Receta creada exitosamente", RecipeMapper.toResponse(savedRecipe));
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getAllRecipes() {
        List<RecipeResponse> recipes = recipeRepository.findAll().stream()
                .map(RecipeMapper::toResponse)
                .collect(Collectors.toList());  
        
        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<RecipeResponse> getRecipeById(Long id) {

        Optional<Recipe> recipeOpt = recipeRepository.findById(id);

        if (recipeOpt.isEmpty()) {
            return Result.error("Recipe not found");
        }

        Recipe recipe = recipeOpt.get();

        // RecipeResponse dto = recipeMapper.toResponse(recipe);
        RecipeResponse dto = RecipeMapper.toResponse(recipe);

        // ⭐ Obtener promedio
        double avg = ratingService.getAverageRating(recipe.getId());
        dto.setAverageRating(avg);

        // ⭐ Obtener conteos por estrella
        Map<Integer, Long> counts = ratingService.getRatingCounts(recipe.getId());
        dto.setRatingCounts(counts);

        // ⭐ Valoración del usuario activo (si no tienes login, pon un userId fijo temporal)
        Long loggedUserId = 1L; // temporal, igual que en los comentarios
        Optional<Rating> userRatingOpt = ratingService.getUserRating(loggedUserId, recipe.getId());
        dto.setUserRating(userRatingOpt.map(Rating::getStars).orElse(null));

        return Result.success("OK", dto);
    }


    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipeByName(String name) {
        List<RecipeResponse> recipes = recipeRepository.findByName(name)
            .stream()
            .map(RecipeMapper::toResponse)
            .collect(Collectors.toList());

        return Result.success("Receta encontrada exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipesByDifficulty(String difficulty) {
        List<RecipeResponse> recipes = recipeRepository.findByDifficulty(difficulty.toUpperCase())
                .stream()
                .map(RecipeMapper::toResponse)
                .collect(Collectors.toList());

        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional
    public Result<RecipeResponse> updateRecipe(Long id, UpdateRecipeRequest request) {
        Optional<Recipe> existingRecipe = recipeRepository.findById(id);

        if (!existingRecipe.isPresent())
            Result.failure("Receta no encontrada");

        Recipe updatedRecipe = RecipeMapper.updateEntity(existingRecipe.get(), request);
        Recipe savedRecipe = recipeRepository.save(updatedRecipe);

        return Result.success("Receta actualizada exitosamente", RecipeMapper.toResponse(savedRecipe));
    }

    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    } 

    /**
     * Simula la búsqueda de recetas aplicando varios filtros.
     * @param query Texto libre para buscar en nombre o ingredientes.
     * @param type Tipo de cocina (e.g., "Mexicana", "Italiana").
     * @param country País de origen.
     * @param difficulty Nivel de dificultad.
     * @return Lista de RecipeResponse filtradas.
     */
    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> searchRecipes(String query, String type, String country, DifficultyLevel difficulty) {
        
        final String lowerCaseQuery = (query != null) ? query.toLowerCase() : "";

        List<RecipeResponse> recipes = recipeRepository
                .findAllWithDetails()
                .stream()
                .map(RecipeMapper::toResponse)
                .filter(recipe -> lowerCaseQuery.isEmpty() || 
                                   recipe.getName().toLowerCase().contains(lowerCaseQuery) ||
                                   recipe.getIngredients().stream().anyMatch(
                                        ri -> ri.getIngredient().getName().toLowerCase().contains(lowerCaseQuery)
                                    ))
                .filter(recipe -> type == null || type.isEmpty() || type.equalsIgnoreCase(recipe.getType()))
                .filter(recipe -> country == null || country.isEmpty() || country.equalsIgnoreCase(recipe.getCountry()))
                .filter(recipe -> difficulty == null || difficulty == recipe.getDifficulty())
                .collect(Collectors.toList());

        return Result.success("Recetas encontradas exitosamente", recipes);
    }
}
