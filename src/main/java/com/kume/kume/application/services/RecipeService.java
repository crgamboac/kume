package com.kume.kume.application.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.Rating;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.RecipeMediaType;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.presentation.mappers.RecipeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RatingService ratingService;
    // private final RecipeMapper recipeMapper;
    @Autowired
    private final RecipeRepository recipeRepository;

    @Autowired
    private final RecipeMediaService recipeMediaService;

    @Autowired
    private final RecipeMapper recipeMapper;

    @Transactional
public Result<RecipeResponse> createRecipe(CreateRecipeRequest request) throws IOException {

    // 1. Mapear receta sin imágenes aún
    Recipe recipe = recipeMapper.toEntity(request);

    // 2. Guardar receta primero (para obtener ID)
    Recipe savedRecipe = recipeRepository.save(recipe);

    // ============================================================
    // 3. GUARDAR IMAGEN PRINCIPAL
    // ============================================================
    MultipartFile cover = request.getImageFile();

    if (cover != null && !cover.isEmpty()) {

        String filename = recipeMediaService.saveMedia(cover); // guarda en /uploads/recipes

        savedRecipe.setImageUrl(filename);
    }

    // ============================================================
    // 4. GUARDAR IMÁGENES EXTRAS
    // ============================================================
    if (request.getExtraImages() != null) {
        for (MultipartFile file : request.getExtraImages()) {

            if (file == null || file.isEmpty()) continue;

            String filename = recipeMediaService.saveMedia(file);

            RecipeMediaType type = resolveMediaType(file.getContentType());

            RecipeMediaDTO dto = RecipeMediaDTO.builder()
                    .recipeId(savedRecipe.getId())
                    .mediaUrl(filename)
                    .mediaType(type)
                    .build();

            recipeMediaService.create(dto);
        }
    }

    // 5. Guardar cambios finales (URL de portada actualizada)
    Recipe updated = recipeRepository.save(savedRecipe);

    return Result.success(
            "Receta creada exitosamente",
            recipeMapper.toResponse(updated)
    );
}


    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getAllRecipes() {
        List<RecipeResponse> recipes = recipeRepository.findAll().stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());

        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<RecipeResponse> getRecipeById(Long id) {

        Optional<Recipe> recipeOpt = recipeRepository.findById(id);

        if (recipeOpt.isEmpty()) {
            return Result.error("Recipe not found");
        }

        Recipe recipe = recipeOpt.get();

        // RecipeResponse dto = recipeMapper.toResponse(recipe);
        RecipeResponse dto = recipeMapper.toResponse(recipe);

        // ⭐ Obtener promedio
        double avg = ratingService.getAverageRating(recipe.getId());
        dto.setAverageRating(avg);

        // ⭐ Obtener conteos por estrella
        Map<Integer, Long> counts = ratingService.getRatingCounts(recipe.getId());
        dto.setRatingCounts(counts);

        // ⭐ Valoración del usuario activo (si no tienes login, pon un userId fijo temporal)
        Long loggedUserId = 1L; // temporal, igual que en los comentarios
        Optional<Rating> userRatingOpt = ratingService.getUserRating(loggedUserId, recipe.getId());
        dto.setUserRating(userRatingOpt.map(Rating::getStars).orElse(null));

        return Result.success("OK", dto);
    }


    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipeByName(String name) {
        List<RecipeResponse> recipes = recipeRepository.findByName(name)
                .stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());

        return Result.success("Receta encontrada exitosamente", recipes);
    }

    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> getRecipesByDifficulty(String difficulty) {
        List<RecipeResponse> recipes = recipeRepository.findByDifficulty(difficulty.toUpperCase())
                .stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());

        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    @Transactional
    public Result<RecipeResponse> updateRecipe(Long id, UpdateRecipeRequest request) {
        Optional<Recipe> existingRecipe = recipeRepository.findById(id);

        if (!existingRecipe.isPresent())
            Result.failure("Receta no encontrada");

        Recipe updatedRecipe = recipeMapper.updateEntity(existingRecipe.get(), request);
        Recipe savedRecipe = recipeRepository.save(updatedRecipe);

        return Result.success("Receta actualizada exitosamente", recipeMapper.toResponse(savedRecipe));
    }

    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    /**
     * Simula la búsqueda de recetas aplicando varios filtros.
     * 
     * @param query      Texto libre para buscar en nombre o ingredientes.
     * @param type       Tipo de cocina (e.g., "Mexicana", "Italiana").
     * @param country    País de origen.
     * @param difficulty Nivel de dificultad.
     * @return Lista de RecipeResponse filtradas.
     */
    @Transactional(readOnly = true)
    public Result<List<RecipeResponse>> searchRecipes(String query, String type, String country,
            DifficultyLevel difficulty) {

        final String lowerCaseQuery = (query != null) ? query.toLowerCase() : "";

        List<RecipeResponse> recipes = recipeRepository
                .findAllWithDetails()
                .stream()
                .map(recipeMapper::toResponse)
                .filter(recipe -> lowerCaseQuery.isEmpty() ||
                        recipe.getName().toLowerCase().contains(lowerCaseQuery) ||
                        recipe.getIngredients().stream().anyMatch(
                                ri -> ri.getIngredient().getName().toLowerCase().contains(lowerCaseQuery)))
                .filter(recipe -> type == null || type.isEmpty() || type.equalsIgnoreCase(recipe.getType()))
                .filter(recipe -> country == null || country.isEmpty() || country.equalsIgnoreCase(recipe.getCountry()))
                .filter(recipe -> difficulty == null || difficulty == recipe.getDifficulty())
                .collect(Collectors.toList());

        return Result.success("Recetas encontradas exitosamente", recipes);
    }

    private List<String> saveMediaFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }

        List<String> finalPaths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty())
                continue;

            try {
                String uploadDir = "uploads/recipes/";
                Files.createDirectories(Paths.get(uploadDir));

                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path fullPath = Paths.get(uploadDir, filename);

                Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

                finalPaths.add(fullPath.toString());

            } catch (IOException ex) {
                throw new RuntimeException("Error guardando archivo: " + file.getOriginalFilename(), ex);
            }
        }

        return finalPaths;
    }

    private RecipeMediaType resolveMediaType(String contentType) {
        if (contentType == null) {
            return RecipeMediaType.IMAGE;
        }

        if (contentType.startsWith("image/")) {
            return RecipeMediaType.IMAGE;
        }

        if (contentType.startsWith("video/")) {
            return RecipeMediaType.VIDEO;
        }

        return RecipeMediaType.IMAGE;
    }

}
