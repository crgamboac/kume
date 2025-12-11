package com.kume.kume.application.presentation.mappers;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.RecipeMedia;
import com.kume.kume.infraestructure.models.RecipeMediaType;
import com.kume.kume.presentation.mappers.RecipeMediaMapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeMediaMapperTest {

    @Test
    void testFromEntity() {
        Recipe recipe = Recipe.builder().id(50L).build();
        
        RecipeMedia entity = RecipeMedia.builder()
                .id(1L)
                .recipe(recipe)
                .mediaUrl("http://video.com/vid.mp4")
                .mediaType(RecipeMediaType.VIDEO)
                .build();

        RecipeMediaDTO dto = RecipeMediaMapper.fromEntity(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(50L, dto.getRecipeId()); // Verificamos que extrajo el ID de la receta
        assertEquals("http://video.com/vid.mp4", dto.getMediaUrl());
        assertEquals(RecipeMediaType.VIDEO, dto.getMediaType());
    }

    @Test
    void testToEntity() {
        RecipeMediaDTO dto = RecipeMediaDTO.builder()
                .id(2L)
                .recipeId(100L)
                .mediaUrl("http://img.com/foto.jpg")
                .mediaType(RecipeMediaType.IMAGE)
                .build();

        RecipeMedia entity = RecipeMediaMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(2L, entity.getId());
        assertNotNull(entity.getRecipe());
        assertEquals(100L, entity.getRecipe().getId()); 
        assertEquals("http://img.com/foto.jpg", entity.getMediaUrl());
        assertEquals(RecipeMediaType.IMAGE, entity.getMediaType());
    }

    @Test
    void testConstructor() {
        RecipeMediaMapper mapper = new RecipeMediaMapper();
        assertNotNull(mapper);
    }
}
