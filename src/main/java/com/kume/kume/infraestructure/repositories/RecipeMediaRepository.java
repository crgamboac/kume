package com.kume.kume.infraestructure.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kume.kume.infraestructure.models.RecipeMedia;

@Repository
public interface RecipeMediaRepository extends JpaRepository<RecipeMedia, Long> {
    List<RecipeMedia> findByRecipeId(Long recipeId);

    List<RecipeMedia> findByMediaType(String mediaType);

}
