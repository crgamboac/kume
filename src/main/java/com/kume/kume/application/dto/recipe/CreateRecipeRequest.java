package com.kume.kume.application.dto.recipe;

import java.util.List;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.kume.kume.infraestructure.models.DifficultyLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "El tiempo de cocina es obligatorio")
    @Positive(message = "El tiempo debe ser mayor a 0")
    private Long cookingTime;

    @NotNull(message = "La dificultad de la receta es obligatoria")
    private DifficultyLevel difficulty;

    @NotBlank(message = "El tipo de cocina es obligatorio")
    private String type;

    @NotBlank(message = "El país de origen es obligatorio")
    private String country;

    private String imageUrl;

    private MultipartFile imageFile;

    @NotNull(message = "Debe ingresar ingredientes")
    @NotEmpty(message = "Debe ingresar al menos un ingrediente")
    private List<CreateRecipeIngredientRequest> ingredients;

    @NotNull(message = "Debe ingresar pasos")
    @NotEmpty(message = "Debe ingresar al menos un paso")
    private List<CreateStepRequest> steps;

    private List<MultipartFile> extraImages;

    private Long userId;
}
