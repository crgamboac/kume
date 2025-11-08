package com.kume.kume.application.dto.recipe;

import java.util.Set;

import com.kume.kume.infraestructure.models.DifficultyLevel;

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
public class CreateRecipeRequest {
    @NotBlank(message = "El nombre de la receta es obligatorio")
    private String name;
    @NotBlank(message = "El tiempo de cocina es obligatorio")
    private Long cookingTime;
    @NotBlank(message = "La dificultad de la receta es obligatoria")
    private DifficultyLevel difficulty;
    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String imageUrl;
    @NotNull(message = "La lista de ingredientes no debe ser nula")
    @NotEmpty(message = "Los ingredientes son obligatorios")
    private Set<CreateRecipeIngredientRequest> ingredients;
    @NotNull(message = "La lista de pasos no debe ser nula")
    @NotEmpty(message = "Los pasos son obligatorios")
    private Set<CreateStepRequest> steps;
}
