package com.kume.kume.application.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
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
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.Ingredient;
import com.kume.kume.infraestructure.models.Rating;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.RecipeIngredient;
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
    private RatingService ratingService;

    @InjectMocks
    private RecipeService recipeService;

    private RecipeIngredient mockRecipeIngredient(String name) {
        RecipeIngredient ri = mock(RecipeIngredient.class);
        Ingredient ingredient = mock(Ingredient.class);

        // Uso de Mockito.lenient() para prevenir UnnecessaryStubbing si el filtro no es activado
        Mockito.lenient().when(ri.getIngredient()).thenReturn(ingredient);
        Mockito.lenient().when(ingredient.getName()).thenReturn(name);
        return ri;
    }

    // --- Tests para createRecipe ---
    @Test
    void createRecipe_WithCoverAndExtras_ShouldSaveAndUploadAll() throws IOException {
        // ARRANGE
        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile mockCover = mock(MultipartFile.class);
        MultipartFile mockExtra = mock(MultipartFile.class);

        request.setImageFile(mockCover);
        request.setExtraImages(List.of(mockExtra));

        when(mockCover.isEmpty()).thenReturn(false);
        when(mockExtra.isEmpty()).thenReturn(false);
        when(mockExtra.getContentType()).thenReturn("video/mp4");

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());

        when(recipeMediaService.saveMedia(mockCover)).thenReturn("cover.jpg");
        when(recipeMediaService.saveMedia(mockExtra)).thenReturn("extra.mp4");

        // ACT
        Result<RecipeResponse> result = recipeService.createRecipe(request);

        // ASSERT
        assertTrue(result.isSuccess());
        // Se espera 1 llamada a save. La segunda actualización de URL se gestiona por la transacción.
        verify(recipeRepository, times(1)).save(any(Recipe.class));
        verify(recipeMediaService).saveMedia(mockCover);
        verify(recipeMediaService).saveMedia(mockExtra);
        verify(recipeMediaService).create(any(RecipeMediaDTO.class));
    }

    @Test
    void createRecipe_NoImages_ShouldSaveWithoutUploads() throws IOException {
        // ARRANGE
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setImageFile(null);
        request.setExtraImages(null);

        Recipe recipe = new Recipe();

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());

        // ACT
        recipeService.createRecipe(request);

        // ASSERT
        verify(recipeMediaService, never()).saveMedia(any());
        verify(recipeRepository, times(1)).save(recipe);
    }

    @Test
    void createRecipe_WithEmptyExtraImages_ShouldSkipEmptyFiles() throws IOException {
        // ARRANGE
        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        request.setExtraImages(List.of(emptyFile));
        request.setImageFile(null);

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());

        // ACT
        recipeService.createRecipe(request);

        // ASSERT
        verify(recipeMediaService, never()).saveMedia(any());
        verify(recipeRepository, times(1)).save(recipe);
    }

    @Test
    void createRecipe_WithEmptyCoverAndNullExtra_ShouldHandleEdgeCases() throws IOException {
        // ARRANGE
        CreateRecipeRequest request = new CreateRecipeRequest();

        MultipartFile emptyCover = mock(MultipartFile.class);
        when(emptyCover.isEmpty()).thenReturn(true);
        request.setImageFile(emptyCover);

        request.setExtraImages(Arrays.asList((MultipartFile) null));

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(any())).thenReturn(new RecipeResponse());

        // ACT
        recipeService.createRecipe(request);

        // ASSERT
        verify(recipeMediaService, never()).saveMedia(any());
        verify(recipeRepository, times(1)).save(recipe);
    }

    @Test
    void createRecipe_ExtraImageWithNullContentType_ShouldResolveToImage() throws IOException {
        // ARRANGE
        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile mockExtra = mock(MultipartFile.class);

        request.setExtraImages(List.of(mockExtra));
        request.setImageFile(null);

        when(mockExtra.isEmpty()).thenReturn(false);
        when(mockExtra.getContentType()).thenReturn(null); // Caso: contentType es null

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());
        when(recipeMediaService.saveMedia(mockExtra)).thenReturn("extra.bin");

        // ACT
        recipeService.createRecipe(request);

        // ASSERT
        verify(recipeMediaService).create(any(RecipeMediaDTO.class));
    }

    @Test
    void createRecipe_ExtraImageWithUnknownContentType_ShouldResolveToImage() throws IOException {
        // ARRANGE
        CreateRecipeRequest request = new CreateRecipeRequest();
        MultipartFile mockExtra = mock(MultipartFile.class);

        request.setExtraImages(List.of(mockExtra));
        request.setImageFile(null);

        when(mockExtra.isEmpty()).thenReturn(false);
        when(mockExtra.getContentType()).thenReturn("application/octet-stream"); // Caso: Contenido no imagen/video

        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeMapper.toEntity(request)).thenReturn(recipe);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(new RecipeResponse());
        when(recipeMediaService.saveMedia(mockExtra)).thenReturn("extra.data");

        // ACT
        recipeService.createRecipe(request);

        // ASSERT
        verify(recipeMediaService).create(any(RecipeMediaDTO.class));
    }

    // --- Tests para getAllRecipes ---
    @Test
    void getAllRecipes_RecipesFound_ShouldReturnSuccessWithList() {
        // ARRANGE
        Recipe recipe1 = new Recipe();
        Recipe recipe2 = new Recipe();
        List<Recipe> recipes = List.of(recipe1, recipe2);
        RecipeResponse response1 = new RecipeResponse();
        RecipeResponse response2 = new RecipeResponse();

        when(recipeRepository.findAll()).thenReturn(recipes);
        when(recipeMapper.toResponse(recipe1)).thenReturn(response1);
        when(recipeMapper.toResponse(recipe2)).thenReturn(response2);

        // ACT
        Result<List<RecipeResponse>> result = recipeService.getAllRecipes();

        // ASSERT
        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(recipeRepository).findAll();
    }

    @Test
    void getAllRecipes_NoRecipesFound_ShouldReturnSuccessWithEmptyList() {
        // ARRANGE
        when(recipeRepository.findAll()).thenReturn(Collections.emptyList());

        // ACT
        Result<List<RecipeResponse>> result = recipeService.getAllRecipes();

        // ASSERT
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        verify(recipeRepository).findAll();
    }

    // --- Tests para getRecipeById ---
    @Test
    void getRecipeById_RecipeFound_ShouldReturnSuccessWithDetails() {
        // ARRANGE
        Long recipeId = 1L;
        Recipe recipe = new Recipe();
        recipe.setId(recipeId);
        RecipeResponse response = new RecipeResponse();
        Rating userRating = new Rating();
        userRating.setStars(4);
        Map<Integer, Long> ratingCounts = Map.of(5, 10L, 4, 5L);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponse(recipe)).thenReturn(response);
        when(ratingService.getAverageRating(recipeId)).thenReturn(4.5);
        when(ratingService.getRatingCounts(recipeId)).thenReturn(ratingCounts);
        when(ratingService.getUserRating(eq(1L), eq(recipeId))).thenReturn(Optional.of(userRating));

        // ACT
        Result<RecipeResponse> result = recipeService.getRecipeById(recipeId);

        // ASSERT
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(4.5, result.getData().getAverageRating());
        assertEquals(ratingCounts, result.getData().getRatingCounts());
        assertEquals(4, result.getData().getUserRating());
        verify(recipeRepository).findById(recipeId);
        verify(ratingService).getAverageRating(recipeId);
        verify(ratingService).getRatingCounts(recipeId);
        verify(ratingService).getUserRating(eq(1L), eq(recipeId));
    }

    @Test
    void getRecipeById_RecipeFound_NoUserRating_ShouldReturnSuccessWithNullUserRating() {
        // ARRANGE
        Long recipeId = 1L;
        Recipe recipe = new Recipe();
        recipe.setId(recipeId);
        RecipeResponse response = new RecipeResponse();
        Map<Integer, Long> ratingCounts = Map.of(5, 10L);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponse(recipe)).thenReturn(response);
        when(ratingService.getAverageRating(recipeId)).thenReturn(3.0);
        when(ratingService.getRatingCounts(recipeId)).thenReturn(ratingCounts);
        when(ratingService.getUserRating(eq(1L), eq(recipeId))).thenReturn(Optional.empty());

        // ACT
        Result<RecipeResponse> result = recipeService.getRecipeById(recipeId);

        // ASSERT
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(3.0, result.getData().getAverageRating());
        assertNull(result.getData().getUserRating());
    }

    @Test
    void getRecipeById_RecipeNotFound_ShouldReturnError() {
        // ARRANGE
        Long recipeId = 99L;
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // ACT
        Result<RecipeResponse> result = recipeService.getRecipeById(recipeId);

        // ASSERT
        assertTrue(!result.isSuccess());
        assertEquals("Recipe not found", result.getMessage());
        verify(recipeRepository).findById(recipeId);
        verify(ratingService, never()).getAverageRating(anyLong());
    }

    // --- Tests para getRecipeByName ---
    @Test
    void getRecipeByName_RecipesFound_ShouldReturnSuccessWithList() {
        // ARRANGE
        String name = "Pasta";
        Recipe recipe1 = new Recipe();
        RecipeResponse response1 = new RecipeResponse();

        when(recipeRepository.findByName(name)).thenReturn(List.of(recipe1));
        when(recipeMapper.toResponse(recipe1)).thenReturn(response1);

        // ACT
        Result<List<RecipeResponse>> result = recipeService.getRecipeByName(name);

        // ASSERT
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        verify(recipeRepository).findByName(name);
    }

    @Test
    void getRecipeByName_NoRecipesFound_ShouldReturnSuccessWithEmptyList() {
        // ARRANGE
        String name = "Inexistente";
        when(recipeRepository.findByName(name)).thenReturn(Collections.emptyList());

        // ACT
        Result<List<RecipeResponse>> result = recipeService.getRecipeByName(name);

        // ASSERT
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        verify(recipeRepository).findByName(name);
    }

    // --- Tests para getRecipesByDifficulty ---
    @Test
    void getRecipesByDifficulty_RecipesFound_ShouldReturnSuccessWithList() {
        // ARRANGE
        String difficulty = "EASY";
        Recipe recipe1 = new Recipe();
        RecipeResponse response1 = new RecipeResponse();

        when(recipeRepository.findByDifficulty(difficulty)).thenReturn(List.of(recipe1));
        when(recipeMapper.toResponse(recipe1)).thenReturn(response1);

        // ACT
        Result<List<RecipeResponse>> result = recipeService.getRecipesByDifficulty(difficulty.toLowerCase());

        // ASSERT
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        verify(recipeRepository).findByDifficulty(difficulty);
    }

    @Test
    void getRecipesByDifficulty_NoRecipesFound_ShouldReturnSuccessWithEmptyList() {
        // ARRANGE
        String difficulty = "DIFICULTAD_INEXISTENTE";
        when(recipeRepository.findByDifficulty(difficulty)).thenReturn(Collections.emptyList());

        // ACT
        Result<List<RecipeResponse>> result = recipeService.getRecipesByDifficulty(difficulty);

        // ASSERT
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    // --- Tests para updateRecipe ---
    @Test
    void updateRecipe_RecipeFound_ShouldUpdateAndReturnSuccess() {
        // ARRANGE
        Long recipeId = 1L;
        UpdateRecipeRequest request = new UpdateRecipeRequest();
        Recipe existingRecipe = new Recipe();
        Recipe updatedRecipe = new Recipe();
        Recipe savedRecipe = new Recipe();
        RecipeResponse response = new RecipeResponse();

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(existingRecipe));
        when(recipeMapper.updateEntity(existingRecipe, request)).thenReturn(updatedRecipe);
        when(recipeRepository.save(updatedRecipe)).thenReturn(savedRecipe);
        when(recipeMapper.toResponse(savedRecipe)).thenReturn(response);

        // ACT
        Result<RecipeResponse> result = recipeService.updateRecipe(recipeId, request);

        // ASSERT
        assertTrue(result.isSuccess());
        assertEquals("Receta actualizada exitosamente", result.getMessage());
        verify(recipeRepository).findById(recipeId);
        verify(recipeRepository).save(updatedRecipe);
    }

    @Test
    void updateRecipe_RecipeNotFound_ShouldReturnFailure() {
        // ARRANGE
        Long recipeId = 99L;
        UpdateRecipeRequest request = new UpdateRecipeRequest();

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());

        // ACT
        Result<RecipeResponse> result = recipeService.updateRecipe(recipeId, request);

        // ASSERT
        assertTrue(!result.isSuccess());
        assertEquals("Receta no encontrada", result.getMessage());
        verify(recipeRepository).findById(recipeId);
        verify(recipeMapper, never()).updateEntity(any(), any());
    }

    // --- Tests para deleteRecipe ---
    @Test
    void deleteRecipe_ShouldCallRepositoryDelete() {
        // ARRANGE
        Long recipeId = 1L;

        // ACT
        recipeService.deleteRecipe(recipeId);

        // ASSERT
        verify(recipeRepository).deleteById(recipeId);
    }

    @Test
    void searchRecipes_AllFiltersPresent_ShouldFilterCorrectly() {
        String query = "Chicken";
        String type = "Lunch";
        String country = "Mexico";
        DifficultyLevel difficulty = DifficultyLevel.EASY;

        Recipe recipe = new Recipe();
        RecipeResponse response = new RecipeResponse();
        response.setName("Chicken Taco");
        response.setType(type);
        response.setCountry(country);
        response.setDifficulty(difficulty);
        response.setIngredients(Set.of(mockRecipeIngredient("chicken breast")));

        when(recipeRepository.findAllWithDetails()).thenReturn(List.of(recipe));

        // Aseguramos que el mapper siempre retorne la respuesta configurada, ignorando el argumento si es necesario
        // Se usa lenient para el stub de cualquier Recipe, forzando la inyección de la respuesta en el stream
        Mockito.lenient().when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(response);

        Result<List<RecipeResponse>> result = recipeService.searchRecipes(query, type, country, difficulty);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    void searchRecipes_QueryFilter_ShouldFilterByNameAndIngredient() {
        String query = "rice";

        Recipe r1 = new Recipe();
        Recipe r2 = new Recipe();
        Recipe r3 = new Recipe();

        RecipeResponse res1 = new RecipeResponse();
        res1.setName("Fried Rice");
        res1.setIngredients(Collections.emptySet());

        RecipeResponse res2 = new RecipeResponse();
        res2.setName("Curry");
        res2.setIngredients(Set.of(mockRecipeIngredient("Basmati rice")));

        RecipeResponse res3 = new RecipeResponse();
        res3.setName("Steak");
        res3.setIngredients(Collections.emptySet());

        when(recipeRepository.findAllWithDetails()).thenReturn(List.of(r1, r2, r3));

        // Uso de thenReturn en cadena para asegurar que cada objeto de la lista se mapee correctamente
        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(res1).thenReturn(res2).thenReturn(res3);

        Result<List<RecipeResponse>> result = recipeService.searchRecipes(query, null, null, null);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    @Test
    void searchRecipes_TypeFilter_CaseInsensitiveMatch() {
        String typeFilter = "dinner";
        Recipe r1 = new Recipe();
        Recipe r2 = new Recipe();

        RecipeResponse res1 = new RecipeResponse();
        res1.setType("Dinner");
        res1.setIngredients(Collections.emptySet());

        RecipeResponse res2 = new RecipeResponse();
        res2.setType("Lunch");
        res2.setIngredients(Collections.emptySet());

        when(recipeRepository.findAllWithDetails()).thenReturn(List.of(r1, r2));

        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(res1).thenReturn(res2);

        Result<List<RecipeResponse>> result = recipeService.searchRecipes(null, typeFilter, null, null);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    void searchRecipes_DifficultyFilter_ShouldFilterByEnum() {
        DifficultyLevel difficulty = DifficultyLevel.INTERMEDIATE;
        Recipe r1 = new Recipe();
        Recipe r2 = new Recipe();

        RecipeResponse res1 = new RecipeResponse();
        res1.setDifficulty(DifficultyLevel.INTERMEDIATE);
        res1.setIngredients(Collections.emptySet());

        RecipeResponse res2 = new RecipeResponse();
        res2.setDifficulty(DifficultyLevel.EASY);
        res2.setIngredients(Collections.emptySet());

        when(recipeRepository.findAllWithDetails()).thenReturn(List.of(r1, r2));

        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(res1).thenReturn(res2);

        Result<List<RecipeResponse>> result = recipeService.searchRecipes(null, null, null, difficulty);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }

    @Test
    void searchRecipes_CountryFilter_EmptyStringShouldNotFilter() {
        String countryFilter = "";
        Recipe r1 = new Recipe();
        RecipeResponse res1 = new RecipeResponse();
        res1.setCountry("Chile");
        res1.setIngredients(Collections.emptySet());

        when(recipeRepository.findAllWithDetails()).thenReturn(List.of(r1));
        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(res1);

        Result<List<RecipeResponse>> result = recipeService.searchRecipes(null, null, countryFilter, null);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }
}
