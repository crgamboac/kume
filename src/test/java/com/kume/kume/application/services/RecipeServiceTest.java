package com.kume.kume.application.services;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

   
    @InjectMocks
    private RecipeService recipeService;

    @Test
    void createRecipe_WithCoverAndExtras_ShouldSaveAndUploadAll() throws IOException {

        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile mockCover = mock(MultipartFile.class);
        MultipartFile mockExtra1 = mock(MultipartFile.class);

        when(mockCover.isEmpty()).thenReturn(false);
        when(mockExtra1.isEmpty()).thenReturn(false);
        when(mockExtra1.getContentType()).thenReturn("image/jpeg");

        request.setImageFile(mockCover);
        request.setExtraImages(List.of(mockExtra1));

        Recipe recipeEntity = new Recipe();
        Recipe savedRecipe = new Recipe();
        savedRecipe.setId(1L);

        try (MockedStatic<RecipeMapper> mapperMock = mockStatic(RecipeMapper.class)) {

            mapperMock.when(() -> RecipeMapper.toEntity(request)).thenReturn(recipeEntity);
            mapperMock.when(() -> RecipeMapper.toResponse(any())).thenReturn(new RecipeResponse());

            when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

            when(recipeMediaService.saveMedia(mockCover)).thenReturn("cover-url.jpg");
            when(recipeMediaService.saveMedia(mockExtra1)).thenReturn("extra-url.jpg");

            Result<RecipeResponse> result = recipeService.createRecipe(request);

            assertTrue(result.isSuccess());

            verify(recipeRepository, times(2)).save(any(Recipe.class));

            verify(recipeMediaService).saveMedia(mockCover);

            verify(recipeMediaService).saveMedia(mockExtra1);
            verify(recipeMediaService).create(any(RecipeMediaDTO.class));
        }
    }

    @Test
    void createRecipe_NoImages_ShouldSaveOnceOrTwiceWithoutUploads() throws IOException {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setImageFile(null);
        request.setExtraImages(null);

        Recipe recipeEntity = new Recipe();
        Recipe savedRecipe = new Recipe();
        savedRecipe.setId(1L);

        try (MockedStatic<RecipeMapper> mapperMock = mockStatic(RecipeMapper.class)) {
            mapperMock.when(() -> RecipeMapper.toEntity(request)).thenReturn(recipeEntity);
            mapperMock.when(() -> RecipeMapper.toResponse(any())).thenReturn(new RecipeResponse());

            when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

            recipeService.createRecipe(request);

            verify(recipeMediaService, never()).saveMedia(any());
            verify(recipeMediaService, never()).create(any());

            verify(recipeRepository, atLeastOnce()).save(any(Recipe.class));
        }
    }

    @Test
    void createRecipe_WithEmptyExtraImages_ShouldSkipEmptyFiles() throws IOException {
        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);
        request.setExtraImages(List.of(emptyFile));
        Recipe savedRecipe = new Recipe();
        savedRecipe.setId(1L);

        try (MockedStatic<RecipeMapper> mapperMock = mockStatic(RecipeMapper.class)) {
            mapperMock.when(() -> RecipeMapper.toEntity(request)).thenReturn(new Recipe());
            mapperMock.when(() -> RecipeMapper.toResponse(any())).thenReturn(new RecipeResponse());

            when(recipeRepository.save(any())).thenReturn(savedRecipe);

            recipeService.createRecipe(request);

            verify(recipeMediaService, never()).saveMedia(any());
            verify(recipeMediaService, never()).create(any());
        }
    }
    @Test
    void createRecipe_WithEmptyCoverAndNullExtra_ShouldHandleEdgeCases() throws IOException {
        CreateRecipeRequest request = new CreateRecipeRequest();
        
        MultipartFile emptyCover = mock(MultipartFile.class);
        when(emptyCover.isEmpty()).thenReturn(true);
        request.setImageFile(emptyCover);

        request.setExtraImages(java.util.Arrays.asList((MultipartFile) null));

        Recipe savedRecipe = new Recipe();
        savedRecipe.setId(1L);

        try (MockedStatic<RecipeMapper> mapperMock = mockStatic(RecipeMapper.class)) {
            mapperMock.when(() -> RecipeMapper.toEntity(request)).thenReturn(new Recipe());
            mapperMock.when(() -> RecipeMapper.toResponse(any())).thenReturn(new RecipeResponse());
            
            when(recipeRepository.save(any())).thenReturn(savedRecipe);

            recipeService.createRecipe(request);

            verify(recipeMediaService, never()).saveMedia(any());
        }
    }

}
