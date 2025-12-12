package com.kume.kume.application.dto.recipe;

import com.kume.kume.infraestructure.models.Ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeIngredientRequest {
    @NotNull(message = "El ingrediente es obligatorio")
    private Ingredient ingredient;
    @NotNull(message = "La cantidad es obligatoria")
    private Double quantity;
}
