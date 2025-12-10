package com.kume.kume.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kume.kume.infraestructure.models.Rating;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.RatingRepository;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.infraestructure.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    // ======================================================
    // GET RATINGS FOR RECIPE
    // ======================================================
    @Test
    void getRatingsForRecipe_ShouldReturnList() {
        Rating r1 = new Rating();
        Rating r2 = new Rating();
        when(ratingRepository.findByRecipeId(10L)).thenReturn(List.of(r1, r2));

        List<Rating> result = ratingService.getRatingsForRecipe(10L);

        assertEquals(2, result.size());
        verify(ratingRepository).findByRecipeId(10L);
    }

    // ======================================================
    // RATE RECIPE - VALIDATIONS
    // ======================================================
    @Test
    void rateRecipe_ShouldThrowException_WhenStarsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            ratingService.rateRecipe(1L, 10L, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ratingService.rateRecipe(1L, 10L, 6);
        });
    }

    // ======================================================
    // RATE RECIPE - CREATE NEW RATING
    // ======================================================
    @Test
    void rateRecipe_ShouldCreateNewRating_WhenNotExists() {
        Long userId = 1L;
        Long recipeId = 10L;

        User user = new User();
        user.setId(userId);

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(ratingRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.empty());

        Rating saved = new Rating();
        saved.setUser(user);
        saved.setRecipe(recipe);
        saved.setStars(4);

        when(ratingRepository.save(any(Rating.class))).thenReturn(saved);

        Rating result = ratingService.rateRecipe(userId, recipeId, 4);

        assertEquals(4, result.getStars());
        assertEquals(user, result.getUser());
        assertEquals(recipe, result.getRecipe());

        verify(ratingRepository).save(any(Rating.class));
    }

    // ======================================================
    // RATE RECIPE - UPDATE EXISTING RATING
    // ======================================================
    @Test
    void rateRecipe_ShouldUpdateExisting_WhenFound() {

        Long userId = 1L;
        Long recipeId = 10L;

        User user = new User();
        user.setId(userId);

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);

        Rating existing = new Rating();
        existing.setId(5L);
        existing.setStars(3);
        existing.setUser(user);
        existing.setRecipe(recipe);
        existing.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(ratingRepository.findByUserIdAndRecipeId(userId, recipeId)).thenReturn(Optional.of(existing));
        when(ratingRepository.save(existing)).thenReturn(existing);

        Rating result = ratingService.rateRecipe(userId, recipeId, 5);

        assertEquals(5, result.getStars());
        assertNotNull(result.getUpdatedAt());
        verify(ratingRepository).save(existing);
    }

    // ======================================================
    // GET USER RATING
    // ======================================================
    @Test
    void getUserRating_ShouldReturnOptional_WhenExists() {
        Rating rating = new Rating();
        rating.setStars(4);

        when(ratingRepository.findByUserIdAndRecipeId(1L, 10L))
            .thenReturn(Optional.of(rating));

        Optional<Rating> result = ratingService.getUserRating(1L, 10L);

        assertTrue(result.isPresent());
        assertEquals(4, result.get().getStars());
    }

    @Test
    void getUserRating_ShouldReturnEmpty_WhenNotFound() {
        when(ratingRepository.findByUserIdAndRecipeId(1L, 10L))
            .thenReturn(Optional.empty());

        Optional<Rating> result = ratingService.getUserRating(1L, 10L);

        assertTrue(result.isEmpty());
    }

    // ======================================================
    // GET AVERAGE RATING
    // ======================================================
    @Test
    void getAverageRating_ShouldReturnValue_WhenRepoReturnsValue() {
        when(ratingRepository.getAverageForRecipe(10L)).thenReturn(4.2);

        double avg = ratingService.getAverageRating(10L);

        assertEquals(4.2, avg);
        verify(ratingRepository).getAverageForRecipe(10L);
    }

    @Test
    void getAverageRating_ShouldReturnZero_WhenNull() {
        when(ratingRepository.getAverageForRecipe(10L)).thenReturn(null);

        double avg = ratingService.getAverageRating(10L);

        assertEquals(0.0, avg);
    }

    // ======================================================
    // GET RATING COUNTS
    // ======================================================
    @Test
    void getRatingCounts_ShouldReturnFiveKeysAlways() {

        List<Object[]> raw = List.of(
            new Object[]{5, 4L},
            new Object[]{3, 2L}
        );

        when(ratingRepository.countStarsForRecipe(10L)).thenReturn(raw);

        Map<Integer, Long> result = ratingService.getRatingCounts(10L);

        assertEquals(5, result.size());

        assertEquals(4L, result.get(5));
        assertEquals(2L, result.get(3));

        // los otros deben ser cero
        assertEquals(0L, result.get(1));
        assertEquals(0L, result.get(2));
        assertEquals(0L, result.get(4));

        verify(ratingRepository).countStarsForRecipe(10L);
    }
}
