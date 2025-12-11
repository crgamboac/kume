package com.kume.kume.application.presentation.mappers;

import com.kume.kume.application.dto.recipe.CreateRecipeIngredientRequest;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.CreateStepRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.Ingredient;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.presentation.mappers.RecipeMapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;


class RecipeMapperTest {
    private final RecipeMapper mapper = new RecipeMapper();
    
    @Test
    void testToEntity() {
        CreateRecipeRequest request = CreateRecipeRequest.builder()
            .name("Empanadas de Pollo")
            .cookingTime(120L)
            .difficulty(DifficultyLevel.EXPERT)
            .country("Chile")
            .type("Once")
            .ingredients(List.of(
                CreateRecipeIngredientRequest.builder()
                    .ingredient(Ingredient.builder()
                        .id(1L)
                        .name("Queso")
                        .build()) 
                    .quantity(1.0)
                    .build(),
                CreateRecipeIngredientRequest.builder()
                    .ingredient(Ingredient.builder()
                        .id(2L)
                        .name("Pan")
                        .build()) 
                    .quantity(1.0)
                    .build()
            ))
            .steps(List.of(
                CreateStepRequest.builder()
                    .stepNumber(1L)
                    .instruction("Preparar los ingredientes")
                    .build(),
                CreateStepRequest.builder()
                    .stepNumber(2L)
                    .instruction("Cocinar los ingredientes")
                    .build()
            ))
            .userId(1L)
            .build();


        Recipe entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Empanadas de Pollo", entity.getName());
        assertEquals(120L, entity.getCookingTime());
    }
    
    @Test
    void testToResponse() {
        Recipe entity = new Recipe();
        entity.setName("Empanadas de Pollo");
        entity.setCookingTime(120L);

        RecipeResponse response = mapper.toResponse(entity);

        assertNotNull(response);
        assertEquals("Empanadas de Pollo", response.getName());
        assertEquals(120L, response.getCookingTime());
    }

    @Test
    void testUpdateEntity() {
        CreateRecipeRequest request = CreateRecipeRequest.builder()
            .name("Empanadas de Pollo")
            .cookingTime(120L)
            .difficulty(DifficultyLevel.EXPERT)
            .country("Chile")
            .type("Once")
            .ingredients(List.of(
                CreateRecipeIngredientRequest.builder()
                    .ingredient(Ingredient.builder()
                        .id(1L)
                        .name("Queso")
                        .build()) 
                    .quantity(1.0)
                    .build(),
                CreateRecipeIngredientRequest.builder()
                    .ingredient(Ingredient.builder()
                        .id(2L)
                        .name("Pan")
                        .build()) 
                    .quantity(1.0)
                    .build()
            ))
            .steps(List.of(
                CreateStepRequest.builder()
                    .stepNumber(1L)
                    .instruction("Preparar los ingredientes")
                    .build(),
                CreateStepRequest.builder()
                    .stepNumber(2L)
                    .instruction("Cocinar los ingredientes")
                    .build()
            ))
            .userId(1L)
            .build();

        Recipe entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Empanadas de Pollo", entity.getName());
        assertEquals(120L, entity.getCookingTime());
    } 
}
