package com.kume.kume.presentation.controllers;

import com.kume.kume.application.dto.IngredientDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.services.IngredientService;
import com.kume.kume.application.services.RecipeMediaService;
import com.kume.kume.application.services.RecipeService;
import com.kume.kume.infraestructure.models.DifficultyLevel; // Asumiendo este enum existe
import com.kume.kume.infraestructure.models.RecipeMedia;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.presentation.mappers.RecipeMapper;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/recipes")
@AllArgsConstructor
public class RecipeController {

    @Autowired
    private final RecipeService recipeService;

    @Autowired
    private final IngredientService ingredientService;

    @Autowired
    private final RecipeMediaService recipeMediaService;

    /**
     * Mapeo para la búsqueda y listado de recetas.
     * Usa @RequestParam para capturar los filtros.
     */
    @GetMapping("/list")
    public String listRecipes(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            Model model) {

        List<RecipeResponse> recipes = recipeService.searchRecipes(query, type, country, difficulty).getData();

        model.addAttribute("recipes", recipes);

        // Pasar los filtros actuales para mantenerlos en el formulario
        model.addAttribute("query", query);
        model.addAttribute("type", type);
        model.addAttribute("country", country);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("allDifficulties", DifficultyLevel.values()); // Para el <select> en Thymeleaf

        return "recipe/recipe-list";
    }

    @GetMapping("/{id}/details")
    public String viewRecipeDetails(@PathVariable("id") Long id, Model model) {

        Result<RecipeResponse> recipeOpt = recipeService.getRecipeById(id);

        if (!recipeOpt.isSuccess()) {
            return "redirect:/recipes/list?error=notfound";
        }

        RecipeResponse recipe = recipeOpt.getData();
        model.addAttribute("recipe", recipe);

        // Cargar medias desde la BD
        List<String> extraImageUrls = recipeMediaService.findByRecipeId(id)
                .stream()
                .map(media ->  media.getMediaUrl()) // aquí está lo que debes cambiar
                .toList();

        model.addAttribute("extraImages", extraImageUrls);

        return "recipe/recipe-details";
    }

    // --- Rutas del CRUD (ADMIN/Privadas) ---

    /**
     * Muestra el formulario para crear una nueva receta (Receta).
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("recipeRequest", new CreateRecipeRequest());
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        List<IngredientDTO> ingredients = ingredientService.getAll().getData();
        model.addAttribute("allIngredients", ingredients);
        return "recipe/recipe-create"; // Vista: Formulario de creación
    }

    /**
     * Procesa la solicitud POST para crear una receta.
     * 
     * @throws IOException
     */
    @PostMapping("/create")
    public String createRecipe(@Valid @ModelAttribute("recipeRequest") CreateRecipeRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal User user) throws IOException {

        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario
            return "recipe/recipe-create";
        }

        request.setUserId(user.getId());

        recipeService.createRecipe(request);
        redirectAttributes.addFlashAttribute("successMessage", "Receta registrada exitosamente.");
        return "redirect:/recipes/list";
    }

    /**
     * Muestra el formulario para editar una receta.
     */
    @GetMapping("/{id}/update")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        RecipeResponse recipe = recipeService.getRecipeById(id).getData();

        // Mapea la respuesta a la solicitud de actualización para poblar el formulario
        UpdateRecipeRequest request = RecipeMapper.toUpdateRequest(recipe);

        model.addAttribute("recipeRequest", request);
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        return "recipe/recipe-edit"; // Vista: Formulario de edición
    }

    /**
     * Procesa la solicitud POST para actualizar una receta.
     */
    @PostMapping("/{id}/update")
    public String updateRecipe(@PathVariable("id") Long id,
            @Valid @ModelAttribute("recipeRequest") UpdateRecipeRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario
            return "recipe/recipe-edit";
        }

        recipeService.updateRecipe(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Receta actualizada exitosamente.");
        return "redirect:/recipes/list";
    }

    /**
     * Elimina una receta.
     */
    @PostMapping("/{id}/delete")
    public String deleteRecipe(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        recipeService.deleteRecipe(id);
        redirectAttributes.addFlashAttribute("successMessage", "Receta eliminada exitosamente.");
        return "redirect:/recipes/list";
    }
}