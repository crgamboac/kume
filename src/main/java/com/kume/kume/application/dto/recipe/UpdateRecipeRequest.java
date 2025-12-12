package com.kume.kume.application.dto.recipe;

import java.util.List;
import java.util.Set;

import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.RecipeIngredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeRequest {
    @NotBlank(message = "El nombre de la receta es obligatorio")
    private String name;
    @NotNull(message = "El tiempo de cocina es obligatorio")
    private Long cookingTime;
    @NotNull(message = "La dificultad de la receta es obligatoria")
    private DifficultyLevel difficulty;
    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String imageUrl;
    @NotEmpty(message = "Los ingredientes son obligatorios")
    private List<RecipeIngredient> ingredients;
}
