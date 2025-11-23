package com.kume.kume.application.dto.recipe;

import java.util.Map;
import java.util.Set;

import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.RecipeIngredient;
import com.kume.kume.infraestructure.models.Step;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {
    private Long id;
    private String name;
    private Long cookingTime;
    private DifficultyLevel difficulty;
    private String imageUrl;
    private String type;
    private String country;
    private Set<RecipeIngredient> ingredients;
    private Set<Step> steps;

    private double averageRating;
    private Map<Integer, Long> ratingCounts;
    private Integer userRating;
}

