package com.kume.kume.application.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.presentation.mappers.RecipeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {

    @Autowired
    private final RecipeRepository recipeRepository;

    @Transactional
    public Result<RecipeResponse> createRecipe(CreateRecipeRequest request) {
        Recipe recipe = RecipeMapper.toEntity(request);
        Recipe savedRecipe = recipeRepository.save(recipe);
        
        return Result.success("Receta creada exitosamente", RecipeMapper.toResponse(savedRecipe));
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getAllRecipes() {
        List<RecipeResponse> recipes = recipeRepository.findAll().stream()
                .map(RecipeMapper::toResponse)
                .collect(Collectors.toList());  
        
        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<RecipeResponse> getRecipeById(Long id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);

        if (!recipe.isPresent())
            return Result.failure("Receta no encontrada");

        return Result.success("Receta encontrada exitosamente", RecipeMapper.toResponse(recipe.get()));
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipeByName(String name) {
        List<RecipeResponse> recipes = recipeRepository.findByName(name)
            .stream()
            .map(RecipeMapper::toResponse)
            .collect(Collectors.toList());

        return Result.success("Receta encontrada exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipesByDifficulty(String difficulty) {
        List<RecipeResponse> recipes = recipeRepository.findByDifficulty(difficulty.toUpperCase())
                .stream()
                .map(RecipeMapper::toResponse)
                .collect(Collectors.toList());

        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional
    public Result<RecipeResponse> updateRecipe(Long id, UpdateRecipeRequest request) {
        Optional<Recipe> existingRecipe = recipeRepository.findById(id);

        if (!existingRecipe.isPresent())
            Result.failure("Receta no encontrada");

        Recipe updatedRecipe = RecipeMapper.updateEntity(existingRecipe.get(), request);
        Recipe savedRecipe = recipeRepository.save(updatedRecipe);

        return Result.success("Receta actualizada exitosamente", RecipeMapper.toResponse(savedRecipe));
    }

    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    } 
}
