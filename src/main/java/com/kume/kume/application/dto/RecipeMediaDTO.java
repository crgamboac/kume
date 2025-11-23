package com.kume.kume.application.dto;

import com.kume.kume.infraestructure.models.RecipeMediaType;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMediaDTO {
    private Long id;
    private Long recipeId;
    private String mediaUrl;
    private RecipeMediaType mediaType;
}
