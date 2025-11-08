package com.kume.kume.presentation.mappers;

import java.util.stream.Collectors;

import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.infraestructure.models.Recipe;

public class RecipeMapper {
    public static RecipeResponse toResponse(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .build();
    }

    public static Recipe toEntity(CreateRecipeRequest request) {
        if (request == null) {
            return null;
        }
        
        return Recipe.builder()
                .name(request.getName())
                .cookingTime(request.getCookingTime())
                .difficulty(request.getDifficulty())
                .imageUrl(request.getImageUrl())
                .ingredients(request.getIngredients()
                    .stream().map(RecipeIngredientMapper::toEntity)
                    .collect(Collectors.toSet()))
                .steps(request.getSteps()
                    .stream().map(StepMapper::toEntity)
                    .collect(Collectors.toSet()))
                .build();
    }
    
    public static Recipe updateEntity(Recipe existingRecipe, UpdateRecipeRequest request) {
        if (request == null) {
            return null;
        }

        existingRecipe.setName(request.getName());
        existingRecipe.setCookingTime(request.getCookingTime());
        existingRecipe.setDifficulty(request.getDifficulty());
        existingRecipe.setImageUrl(request.getImageUrl());
        existingRecipe.setIngredients(request.getIngredients());
        return existingRecipe;
    } 

    public static UpdateRecipeRequest toUpdateRequest(RecipeResponse recipe) {
        if (recipe == null) {
            return null;
        }
        return UpdateRecipeRequest.builder()
                .name(recipe.getName())
                .build();
    }
}     
