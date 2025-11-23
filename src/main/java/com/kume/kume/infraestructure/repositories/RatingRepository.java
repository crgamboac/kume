package com.kume.kume.infraestructure.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kume.kume.infraestructure.models.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndRecipeId(Long userId, Long recipeId);
    List<Rating> findByRecipeId(Long recipeId);
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.recipe.id = :recipeId")
    Double getAverageForRecipe(Long recipeId);

    @Query("SELECT r.stars AS stars, COUNT(r) AS total FROM Rating r WHERE r.recipe.id = :recipeId GROUP BY r.stars")
    List<Object[]> countStarsForRecipe(Long recipeId);
}

