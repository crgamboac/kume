package com.kume.kume.application.services;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.infraestructure.models.RecipeMedia;
import com.kume.kume.infraestructure.models.RecipeMediaType;
import com.kume.kume.infraestructure.repositories.RecipeMediaRepository;
import com.kume.kume.presentation.mappers.RecipeMediaMapper;
import com.kume.kume.infraestructure.models.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecipeMediaServiceTest {

    @Mock
    private RecipeMediaRepository recipeMediaRepository;

    @InjectMocks
    private RecipeMediaService recipeMediaService;

    // Carpeta temporal para no usar uploads reales
    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Reemplazar campo uploadPath con la carpeta temporal
        Field uploadPathField = RecipeMediaService.class.getDeclaredField("uploadPath");
        uploadPathField.setAccessible(true);
        uploadPathField.set(recipeMediaService, tempUploadDir);
    }

    // ------------------------------------------------------------------------------------
    @Test
    void testGetAll() {
        List<RecipeMedia> mockList = List.of(
                new RecipeMedia(1L, new Recipe(), "img1.jpg", RecipeMediaType.IMAGE),
                new RecipeMedia(2L, new Recipe(), "img2.jpg", RecipeMediaType.IMAGE)
        );

        when(recipeMediaRepository.findAll()).thenReturn(mockList);

        Result<List<RecipeMediaDTO>> result = recipeMediaService.getAll();

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(recipeMediaRepository).findAll();
    }


    @Test
    void testGetByRecipeId() {
        List<RecipeMedia> mockList = List.of(
                new RecipeMedia(99L, new Recipe(), "img.png", RecipeMediaType.IMAGE)
        );

        when(recipeMediaRepository.findByRecipeId(10L)).thenReturn(mockList);

        Result<List<RecipeMediaDTO>> result = recipeMediaService.getByRecipeId(10L);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        verify(recipeMediaRepository).findByRecipeId(10L);
    }
    
    @Test
    void testGetById_Found() {
        RecipeMedia media =
                new RecipeMedia(5L, new Recipe(), "media.mp4", RecipeMediaType.VIDEO);

        when(recipeMediaRepository.findById(5L)).thenReturn(Optional.of(media));

        Result<RecipeMediaDTO> result = recipeMediaService.getById(5L);

        assertTrue(result.isSuccess());
        assertEquals("media.mp4", result.getData().getMediaUrl());
    }


    @Test
    void testGetById_NotFound() {
        when(recipeMediaRepository.findById(5L)).thenReturn(Optional.empty());

        Result<RecipeMediaDTO> result = recipeMediaService.getById(5L);

        assertFalse(result.isSuccess());
        assertEquals("La media no existe", result.getMessage());
    }

    // ------------------------------------------------------------------------------------
    @Test
    void testCreate() {
        RecipeMediaDTO dto = new RecipeMediaDTO();
        dto.setMediaUrl("img.jpg");
        dto.setMediaType(RecipeMediaType.IMAGE);
        dto.setRecipeId(1L);

        RecipeMedia entity = RecipeMediaMapper.toEntity(dto);

        when(recipeMediaRepository.save(any())).thenReturn(entity);

        Result<RecipeMediaDTO> result = recipeMediaService.create(dto);

        assertTrue(result.isSuccess());
        assertEquals("img.jpg", result.getData().getMediaUrl());
        verify(recipeMediaRepository).save(any(RecipeMedia.class));
    }



    @Test
    void testUpdate_NotFound() {
        when(recipeMediaRepository.findById(1L)).thenReturn(Optional.empty());

        Result<RecipeMediaDTO> result = recipeMediaService.update(1L, new RecipeMediaDTO());

        assertFalse(result.isSuccess());
        assertEquals("No se encontró la media para actualizar", result.getMessage());
    }

    // ------------------------------------------------------------------------------------
    @Test
    void testDelete_Found() {
        RecipeMedia media = new RecipeMedia();
        when(recipeMediaRepository.findById(10L)).thenReturn(Optional.of(media));

        Result<Void> result = recipeMediaService.delete(10L);

        assertTrue(result.isSuccess());
        verify(recipeMediaRepository).delete(media);
    }

    @Test
    void testDelete_NotFound() {
        when(recipeMediaRepository.findById(10L)).thenReturn(Optional.empty());

        Result<Void> result = recipeMediaService.delete(10L);

        assertFalse(result.isSuccess());
        assertEquals("No se encontró la media para eliminar", result.getMessage());
    }
    
    @Test
    void testUpdate_Found() {
        Recipe existingRecipe = new Recipe();
        existingRecipe.setId(1L);

        RecipeMedia existing = new RecipeMedia(
                1L,
                existingRecipe,
                "old.png",
                RecipeMediaType.IMAGE
        );

        RecipeMediaDTO dto = new RecipeMediaDTO(
                1L,
                3L,                    // recipeId
                "new.png",
                RecipeMediaType.IMAGE
        );

        when(recipeMediaRepository.findById(1L)).thenReturn(Optional.of(existing));

        Result<RecipeMediaDTO> result = recipeMediaService.update(1L, dto);

        assertTrue(result.isSuccess());
        assertEquals("new.png", result.getData().getMediaUrl());
        assertEquals(3L, existing.getRecipe().getId());

        verify(recipeMediaRepository).save(existing);
    }

    // ------------------------------------------------------------------------------------
    @Test
    void testSaveMedia() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto original.png",
                "image/png",
                "contenido".getBytes()
        );

        String saved = recipeMediaService.saveMedia(file);

        // Verifica que se generó un archivo en la carpeta temporal
        Path expectedPath = tempUploadDir.resolve(saved);

        assertTrue(Files.exists(expectedPath));
        assertTrue(saved.contains("_foto_original.png")); // filename limpio
    }
}
