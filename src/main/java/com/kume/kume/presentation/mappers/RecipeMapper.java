package com.kume.kume.presentation.mappers;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.RecipeIngredient;
import com.kume.kume.infraestructure.models.Step;
import com.kume.kume.infraestructure.models.User;
@Component
public class RecipeMapper {
    public  RecipeResponse toResponse(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeResponse recipeResponse = RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .cookingTime(recipe.getCookingTime())
                .difficulty(recipe.getDifficulty())
                .imageUrl(recipe.getImageUrl())
                .country(recipe.getCountry())
                .type(recipe.getType())
                .steps(recipe.getSteps())
                .ingredients(recipe.getIngredients())
                // estos se llenan luego en RecipeService
                .averageRating(0)
                .ratingCounts(null)
                .userRating(null)
                .build();

        if (recipe.getIngredients() != null) {
            Set<RecipeIngredient> ingredientDTOs = recipe.getIngredients().stream()
                    .collect(Collectors.toSet());
            recipeResponse.setIngredients(ingredientDTOs);
        } else {
            recipe.setIngredients(Collections.emptySet()); // Asegurar que NUNCA sea NULL
        }

        return recipeResponse;
    }

    public  Recipe toEntity(CreateRecipeRequest request) {
        if (request == null)
            return null;

        Recipe recipe = Recipe.builder()
                .name(request.getName())
                .cookingTime(request.getCookingTime())
                .difficulty(request.getDifficulty())
                .imageUrl(request.getImageUrl())
                .country(request.getCountry())
                .type(request.getType())
                .build();

        User user = User.builder().id(request.getUserId()).build();

        recipe.setUser(user);
        
        Set<RecipeIngredient> ingredients = request.getIngredients().stream()
                .map(r -> {
                    RecipeIngredient ri = RecipeIngredientMapper.toEntity(r);
                    ri.setRecipe(recipe); // ← VÍNCULO OBLIGATORIO
                    return ri;
                })
                .collect(Collectors.toSet());

        Set<Step> steps = request.getSteps().stream()
                .map(s -> {
                    Step step = StepMapper.toEntity(s);
                    step.setRecipe(recipe); // ← también requerido
                    return step;
                })
                .collect(Collectors.toSet());

        recipe.setIngredients(ingredients);
        recipe.setSteps(steps);

        return recipe;
    }

    public  Recipe updateEntity(Recipe existingRecipe, UpdateRecipeRequest request) {
        if (request == null) {
            return null;
        }

        existingRecipe.setName(request.getName());
        existingRecipe.setCookingTime(request.getCookingTime());
        existingRecipe.setDifficulty(request.getDifficulty());
        existingRecipe.setImageUrl(request.getImageUrl());
        existingRecipe.setIngredients(Set.copyOf(request.getIngredients()));
        return existingRecipe;
    }

    public  UpdateRecipeRequest toUpdateRequest(RecipeResponse recipe) {
        if (recipe == null) {
            return null;
        }
        return UpdateRecipeRequest.builder()
                .name(recipe.getName())
                .build();
    }
}
