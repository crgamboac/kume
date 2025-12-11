package com.kume.kume.application.presentation.mappers;

import com.kume.kume.application.dto.recipe.CreateRecipeIngredientRequest;
import com.kume.kume.infraestructure.models.Ingredient;
import com.kume.kume.infraestructure.models.RecipeIngredient;
import com.kume.kume.presentation.mappers.RecipeIngredientMapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeIngredientMapperTest {

    @Test
    void testToEntity_Success() {
        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Harina")
                .build();

        CreateRecipeIngredientRequest request = CreateRecipeIngredientRequest.builder()
                .ingredient(ingredient)
                .quantity(500.0)
                .build();

        RecipeIngredient result = RecipeIngredientMapper.toEntity(request);

        assertNotNull(result);
        assertEquals(500.0, result.getQuantity());
        assertEquals("Harina", result.getIngredient().getName());
        assertEquals(1L, result.getIngredient().getId());
    }

    @Test
    void testToEntity_NullRequest() {
        RecipeIngredient result = RecipeIngredientMapper.toEntity(null);
        
        assertNull(result);
    }

    @Test
    void testConstructor() {
        RecipeIngredientMapper mapper = new RecipeIngredientMapper();
        assertNotNull(mapper);
    }
}
