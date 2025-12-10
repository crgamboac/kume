package com.kume.kume.presentation.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Asumiendo este enum existe
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kume.kume.application.dto.IngredientDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.comment.CommentResponse;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.application.services.CommentService;
import com.kume.kume.application.services.IngredientService;
import com.kume.kume.application.services.RatingService;
import com.kume.kume.application.services.RecipeMediaService;
import com.kume.kume.application.services.RecipeService;
import com.kume.kume.infraestructure.models.DifficultyLevel;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.UserRepository;
import com.kume.kume.presentation.mappers.RecipeMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/recipes")
@AllArgsConstructor
public class RecipeController {

    @Autowired
    private final RecipeService recipeService;
    private final CommentService commentService;
    private final RatingService ratingService;
    private final UserRepository userRepository;
    

    @Autowired
    private final IngredientService ingredientService;

    @Autowired
    private final RecipeMediaService recipeMediaService;

    @Autowired
    private final RecipeMapper recipeMapper;
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
    public String viewRecipeDetails(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        //Obtener el DTO de la receta
        Result<RecipeResponse> recipeOpt = recipeService.getRecipeById(id);
        if (!recipeOpt.isSuccess()) {
            return "redirect:/recipes/list?error=notfound";
        }
        RecipeResponse recipe = recipeOpt.getData();
        // Obtener comentarios raíz para esta receta
        List<CommentResponse> comments = commentService.getRootComments(recipe.getId());
        

        String absoluteUrl = request.getRequestURL().toString();
        
        //Pasar datos a la vista
        model.addAttribute("recipe", recipe);
        model.addAttribute("comments", comments);
        model.addAttribute("shareUrl", absoluteUrl);

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
        UpdateRecipeRequest request = recipeMapper.toUpdateRequest(recipe);

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


    // rating recipe
    @PostMapping("/{id}/rate")
    public String rateRecipe(
            @PathVariable Long id,
            @RequestParam int stars) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + username));

        ratingService.rateRecipe(user.getId(), id, stars);
        return "redirect:/recipes/" + id + "/details";
    }

}