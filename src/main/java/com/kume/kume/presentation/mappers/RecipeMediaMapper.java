package com.kume.kume.presentation.mappers;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.RecipeMedia;

public class RecipeMediaMapper {

    public static RecipeMediaDTO fromEntity(RecipeMedia entity) {
        return RecipeMediaDTO.builder()
                .id(entity.getId())
                .recipeId(entity.getRecipe().getId())
                .mediaUrl(entity.getMediaUrl())
                .mediaType(entity.getMediaType())
                .build();
    }

    public static RecipeMedia toEntity(RecipeMediaDTO dto) {
        return RecipeMedia.builder()
                .id(dto.getId())
                .recipe(
                        Recipe.builder()
                                .id(dto.getRecipeId())
                                .build())
                .mediaUrl(dto.getMediaUrl())
                .mediaType(dto.getMediaType())
                .build();
    }
}
