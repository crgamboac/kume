package com.kume.kume.application.services;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue; // Importante para listas vacías seguras
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.presentation.mappers.RecipeMapper;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMediaService recipeMediaService;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private RatingService ratingService; // <--- ¡ESTO FALTABA!

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void createRecipe_WithCoverAndExtras_ShouldSaveAndUploadAll() throws IOException {
        // --- ARRANGE ---
        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile mockCover = mock(MultipartFile.class);
        MultipartFile mockExtra = mock(MultipartFile.class);

        request.setImageFile(mockCover);
        request.setExtraImages(List.of(mockExtra));

        when(mockCover.isEmpty()).thenReturn(false);
        when(mockExtra.isEmpty()).thenReturn(false);
        when(mockExtra.getContentType()).thenReturn("image/jpeg");

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());

        when(recipeMediaService.saveMedia(mockCover)).thenReturn("cover.jpg");
        when(recipeMediaService.saveMedia(mockExtra)).thenReturn("extra.jpg");

        // --- ACT ---
        Result<RecipeResponse> result = recipeService.createRecipe(request);

        // --- ASSERT ---
        assertTrue(result.isSuccess());
        verify(recipeMapper).toEntity(request);
        verify(recipeRepository).save(recipe); // Se llama al menos una vez
        verify(recipeMediaService).saveMedia(mockCover);
        verify(recipeMediaService).saveMedia(mockExtra);
        verify(recipeMediaService).create(any(RecipeMediaDTO.class));
    }

    @Test
    void createRecipe_NoImages_ShouldSaveWithoutUploads() throws IOException {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setImageFile(null);
        request.setExtraImages(null);

        Recipe recipe = new Recipe();
        
        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());

        recipeService.createRecipe(request);

        verify(recipeMediaService, never()).saveMedia(any());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void createRecipe_WithEmptyExtraImages_ShouldSkipEmptyFiles() throws IOException {
        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        
        request.setExtraImages(List.of(emptyFile));

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());

        recipeService.createRecipe(request);

        verify(recipeMediaService, never()).saveMedia(any());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void createRecipe_WithEmptyCoverAndNullExtra_ShouldHandleEdgeCases() throws IOException {
        CreateRecipeRequest request = new CreateRecipeRequest();
        
        MultipartFile emptyCover = mock(MultipartFile.class);
        when(emptyCover.isEmpty()).thenReturn(true);
        request.setImageFile(emptyCover);

        // Usamos java.util.Arrays.asList para permitir nulos (List.of explota con nulos)
        request.setExtraImages(java.util.Arrays.asList((MultipartFile) null));

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(any())).thenReturn(new RecipeResponse());

        recipeService.createRecipe(request);

        verify(recipeMediaService, never()).saveMedia(any());
        verify(recipeRepository).save(recipe);
    }
}