package com.kume.kume.application.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.RecipeMedia;
import com.kume.kume.infraestructure.repositories.RecipeMediaRepository;
import com.kume.kume.presentation.mappers.RecipeMediaMapper;

@Service
public class RecipeMediaService {

    private final RecipeMediaRepository recipeMediaRepository;
    private final Path uploadPath = Paths.get("uploads/recipes");

    public RecipeMediaService(RecipeMediaRepository recipeMediaRepository) throws IOException {
        this.recipeMediaRepository = recipeMediaRepository;
        Files.createDirectories(uploadPath);
    }

    public Result<List<RecipeMediaDTO>> getAll() {
        List<RecipeMediaDTO> list = recipeMediaRepository.findAll()
                .stream()
                .map(RecipeMediaMapper::fromEntity)
                .collect(Collectors.toList());

        return Result.success("Listado de media obtenido correctamente", list);
    }

    public Result<List<RecipeMediaDTO>> getByRecipeId(Long recipeId) {
        List<RecipeMediaDTO> list = recipeMediaRepository.findByRecipeId(recipeId)
                .stream()
                .map(RecipeMediaMapper::fromEntity)
                .collect(Collectors.toList());

        return Result.success("Media asociada a la receta obtenida", list);
    }

    public Result<RecipeMediaDTO> getById(Long id) {
        Optional<RecipeMedia> media = recipeMediaRepository.findById(id);

        return media
                .map(m -> Result.success("Media encontrada", RecipeMediaMapper.fromEntity(m)))
                .orElseGet(() -> Result.failure("La media no existe"));
    }

    public Result<RecipeMediaDTO> create(RecipeMediaDTO dto) {
        RecipeMedia entity = RecipeMediaMapper.toEntity(dto);
        recipeMediaRepository.save(entity);

        return Result.success("Media creada con éxito", RecipeMediaMapper.fromEntity(entity));
    }

    public Result<RecipeMediaDTO> update(Long id, RecipeMediaDTO dto) {
        Optional<RecipeMedia> media = recipeMediaRepository.findById(id);

        if (media.isEmpty()) {
            return Result.failure("No se encontró la media para actualizar");
        }

        RecipeMedia existing = media.get();
        existing.setMediaUrl(dto.getMediaUrl());
        existing.setMediaType(dto.getMediaType());

        Recipe recipe = new Recipe();
        recipe.setId(dto.getRecipeId());

        existing.setRecipe(recipe);

        recipeMediaRepository.save(existing);

        return Result.success("Media actualizada correctamente", RecipeMediaMapper.fromEntity(existing));
    }

    public Result<Void> delete(Long id) {
        Optional<RecipeMedia> media = recipeMediaRepository.findById(id);

        if (media.isEmpty()) {
            return Result.failure("No se encontró la media para eliminar");
        }

        recipeMediaRepository.delete(media.get());
        return Result.success("Media eliminada correctamente", null);
    }

    public List<RecipeMedia> findByRecipeId(Long recipeId) {
        return recipeMediaRepository.findByRecipeId(recipeId);
    }

    public String saveMedia(MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        String cleanName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = System.currentTimeMillis() + "_" + cleanName;
        Path destination = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), destination);
        return filename;
    }
}