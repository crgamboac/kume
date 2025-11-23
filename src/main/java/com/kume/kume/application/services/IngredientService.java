package com.kume.kume.application.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kume.kume.application.dto.IngredientDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.infraestructure.models.Ingredient;
import com.kume.kume.infraestructure.repositories.IngredientRepository;


@Service
public class IngredientService {
    private IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public Result<List<IngredientDTO>> getAll() {
        List<IngredientDTO> ingredients = ingredientRepository.findAll()
                .stream()
                .map(IngredientDTO::fromEntity)
                .collect(Collectors.toList());

        return Result.success("Listado de ingredientes obtenido correctamente", ingredients);
    }

    public Result<IngredientDTO> getById(Long id) {
        Optional<Ingredient> ingredient = ingredientRepository.findById(id);

        return ingredient
                .map(i -> Result.success("Ingrediente encontrado", IngredientDTO.fromEntity(i)))
                .orElseGet(() -> Result.failure("El ingrediente no existe"));
    }


    public Result<IngredientDTO> create(IngredientDTO dto) {
        Ingredient ingredient = dto.toEntity();
        ingredientRepository.save(ingredient);

        return Result.success("Ingrediente creado con éxito", IngredientDTO.fromEntity(ingredient));
    }

    public Result<IngredientDTO> update(Long id, IngredientDTO dto) {
        Optional<Ingredient> ingredient = ingredientRepository.findById(id);

        if (ingredient.isEmpty()) {
            return Result.failure("No se encontró el ingrediente para actualizar");
        }

        Ingredient existing = ingredient.get();
        existing.setName(dto.getName());
        existing.setIngredientType(dto.getIngredientType());

        ingredientRepository.save(existing);

        return Result.success("Ingrediente actualizado correctamente", IngredientDTO.fromEntity(existing));
    }

    public Result<Void> delete(Long id) {
        Optional<Ingredient> ingredient = ingredientRepository.findById(id);

        if (ingredient.isEmpty()) {
            return Result.failure("No se encontró el ingrediente para eliminar");
        }

        ingredientRepository.delete(ingredient.get());
        return Result.success("Ingrediente eliminado correctamente", null);
    }
}
