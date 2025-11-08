package com.kume.kume.infraestructure.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kume.kume.infraestructure.models.Ingredient;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);
    Optional<List<Ingredient>> findByingredientType(String ingredientType);
}
