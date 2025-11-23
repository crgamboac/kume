package com.kume.kume.application.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kume.kume.infraestructure.models.Rating;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.RatingRepository;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.infraestructure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public List<Rating> getRatingsForRecipe(Long recipeId) {
        return ratingRepository.findByRecipeId(recipeId);
    }

    public Rating rateRecipe(Long userId, Long recipeId, int stars) {

        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new RuntimeException("Recipe not found"));

        // Si ya existe → modificar
        Optional<Rating> existing = ratingRepository.findByUserIdAndRecipeId(userId, recipeId);

        Rating rating;
        if (existing.isPresent()) {
            rating = existing.get();
            rating.setStars(stars);
            rating.setUpdatedAt(LocalDateTime.now());
        } else {
            // crear nuevo
            rating = new Rating();
            rating.setUser(user);
            rating.setRecipe(recipe);
            rating.setStars(stars);
            rating.setCreatedAt(LocalDateTime.now());
            rating.setUpdatedAt(LocalDateTime.now());
        }

        return ratingRepository.save(rating);
    }

    /**
     * Obtener la valoración hecha por un usuario (si existe).
     */
    public Optional<Rating> getUserRating(Long userId, Long recipeId) {
        return ratingRepository.findByUserIdAndRecipeId(userId, recipeId);
    }

    /**
     * Obtener promedio de estrellas de la receta.
     */
    public double getAverageRating(Long recipeId) {
        Double avg = ratingRepository.getAverageForRecipe(recipeId);
        return avg != null ? avg : 0.0;
    }

    /**
     * Obtener conteo por estrellas: mapa {stars → cantidad}.
     */
    public Map<Integer, Long> getRatingCounts(Long recipeId) {

        List<Object[]> rows = ratingRepository.countStarsForRecipe(recipeId);
        Map<Integer, Long> result = new HashMap<>();
        // poner valores existentes
        for (Object[] row : rows) {
            Integer stars = (Integer) row[0];
            Long total = (Long) row[1];
            result.put(stars, total);
        }
        // aseguramos que existan todas las estrellas 1..5
        for (int i = 1; i <= 5; i++) {
            result.putIfAbsent(i, 0L);
        }
        return result;
    }
}

