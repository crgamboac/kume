package com.kume.kume.presentation.controllers;


import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.services.RecipeService;
import com.kume.kume.infraestructure.models.DifficultyLevel; // Asumiendo este enum existe
import com.kume.kume.presentation.mappers.RecipeMapper;

import jakarta.validation.Valid; // Importante para la validación
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;


import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping("/recipes") // Prefijo para todas las rutas del CRUD
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // --- Rutas Públicas (Listado y Búsqueda) ---
    
    /**
     * [Pública] Mapea la URL de BÚSQUEDA/LISTADO.
     * Cumple con el requisito de 'Buscar recetas'.
     */
    @GetMapping({"/search", "/list"}) 
    public String listRecipes(@RequestParam(value = "query", required = false) String query, Model model) {
        
        List<RecipeResponse> recipes;
        
        if (query != null && !query.isEmpty()) {
            recipes = recipeService.getRecipeByName(query).getData();
        } else {
            recipes = recipeService.getAllRecipes().getData();
        }
        
        model.addAttribute("recipes", recipes);
        model.addAttribute("query", query);
        return "recipe/recipe-list";
    }

    /**
     * [Privada] Mapea la URL para VER DETALLES. Requiere autenticación.
     * Cumple con el requisito de 'Visualizar las recetas para tener acceso a los detalles'.
     */
    @GetMapping("/{id}/details") 
    public String viewRecipeDetails(@PathVariable("id") Long id, Model model) {
        RecipeResponse recipe = recipeService.getRecipeById(id)
                .getData(); // Asume que .getData() maneja el Optional y lanza una excepción si falla

        if (recipe == null) {
            // Manejo de error básico (no encontrado)
            return "redirect:/recetas/listado"; 
        }

        model.addAttribute("recipe", recipe);
        return "recipe/recipe-detail"; // Vista: Detalle de receta (Privada)
    }

    // --- Rutas del CRUD (ADMIN/Privadas) ---

    /**
     * Muestra el formulario para crear una nueva receta (Receta).
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("recipeRequest", new CreateRecipeRequest());
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        return "recipe/recipe-create"; // Vista: Formulario de creación
    }

    /**
     * Procesa la solicitud POST para crear una receta.
     */
    @PostMapping("/create")
    public String createRecipe(@Valid @ModelAttribute("recipeRequest") CreateRecipeRequest request, 
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // Si hay errores de validación, vuelve al formulario
            return "recipe/recipe-create";
        }

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
    @GetMapping("/recipes")
    public String privateRecipesList(Model model) {

        model.addAttribute("navbarItems", List.of(
                Map.of("label", "Inicio", "url", "/home"),
                Map.of("label", "Cerrar Sesión", "url", "/auth/logout")));
        model.addAttribute("toastMessage", "Bienvenido, has iniciado sesión correctamente.");
        model.addAttribute("toastType", "success");
        return "recipe/recipe-list";
    }
}