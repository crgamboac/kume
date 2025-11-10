package com.kume.kume.infraestructure.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kume.kume.infraestructure.models.Recipe;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByName(String name);
    List<Recipe> findByDifficulty(String difficulty);
    @Query("SELECT r FROM Recipe r " +
           "LEFT JOIN FETCH r.ingredients ri " +
           "LEFT JOIN FETCH ri.ingredient i " +
           "LEFT JOIN FETCH r.steps s")
    List<Recipe> findAllWithDetails();
}
