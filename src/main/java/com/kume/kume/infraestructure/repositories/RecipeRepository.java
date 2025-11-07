package com.kume.kume.infraestructure.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kume.kume.infraestructure.models.Recipe;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByName(String name);

    Optional<List<Recipe>> findByDifficulty(String difficulty);

}
