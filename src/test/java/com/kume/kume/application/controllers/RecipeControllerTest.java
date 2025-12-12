package com.kume.kume.application.controllers;

import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.CreateRecipeRequest;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.dto.recipe.UpdateRecipeRequest;
import com.kume.kume.application.services.*;
import com.kume.kume.infraestructure.models.RecipeMedia;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.UserRepository;
import com.kume.kume.presentation.controllers.RecipeController;
import com.kume.kume.presentation.mappers.RecipeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecipeControllerTest {

    private MockMvc mockMvc;

    @Mock private RecipeService recipeService;
    @Mock private CommentService commentService;
    @Mock private RatingService ratingService;
    @Mock private UserRepository userRepository;
    @Mock private IngredientService ingredientService;
    @Mock private RecipeMediaService recipeMediaService;
    @Mock private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeController recipeController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(recipeController).build();
    }

    @Test
    void testListRecipes() throws Exception {
        RecipeResponse mockRecipe = new RecipeResponse();
        mockRecipe.setName("Tractor");
        List<RecipeResponse> recipes = Collections.singletonList(mockRecipe);

        when(recipeService.searchRecipes(any(), any(), any(), any()))
                .thenReturn(Result.success("Ok", recipes));

        mockMvc.perform(get("/recipes/list")
                        .param("query", "tractor"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/recipe-list"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(model().attribute("query", "tractor"));
    }

    @Test
    void testViewRecipeDetails_Success() throws Exception {
        Long recipeId = 1L;
        RecipeResponse mockRecipe = new RecipeResponse();
        mockRecipe.setId(recipeId);

        when(recipeService.getRecipeById(recipeId)).thenReturn(Result.success("Ok", mockRecipe));
        when(commentService.getRootComments(recipeId)).thenReturn(Collections.emptyList());
        
        RecipeMedia mediaDTO = RecipeMedia.builder().mediaUrl("http://img.com").build();
        when(recipeMediaService.findByRecipeId(recipeId)).thenReturn(List.of(mediaDTO));

        mockMvc.perform(get("/recipes/{id}/details", recipeId))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/recipe-details"))
                .andExpect(model().attributeExists("recipe"))
                .andExpect(model().attributeExists("shareUrl"))
                .andExpect(model().attributeExists("extraImages"));
    }

    @Test
    void testViewRecipeDetails_NotFound() throws Exception {
        Long recipeId = 99L;
        when(recipeService.getRecipeById(recipeId)).thenReturn(Result.failure("Not found"));

        mockMvc.perform(get("/recipes/{id}/details", recipeId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/list?error=notfound"));
    }

    @Test
    void testShowCreateForm() throws Exception {
        when(ingredientService.getAll()).thenReturn(Result.success("Ok", Collections.emptyList()));

        mockMvc.perform(get("/recipes/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/recipe-create"))
                .andExpect(model().attributeExists("recipeRequest"))
                .andExpect(model().attributeExists("allIngredients"));
    }

    @Test
    void testCreateRecipe_Success() throws Exception {
        mockMvc.perform(post("/recipes/create")
                        .param("name", "Nueva Receta")
                        .param("cookingTime", "45")
                        .param("difficulty", "EASY") 
                        .param("type", "Almuerzo")     
                        .param("country", "Chile")    
                        .param("imageUrl", "http://imagen.com/foto.jpg")
                        
                        .param("ingredients[0].quantity", "1.5") 
                        .param("ingredients[0].ingredient.id", "1") 
                        
                        .param("steps[0].stepNumber", "1")
                        .param("steps[0].instruction", "Cortar las verduras finamente")
                        )
                
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/list"));

        verify(recipeService).createRecipe(any(CreateRecipeRequest.class));
    }
    
    @Test
    void testCreateRecipe_ValidationError() throws Exception {
        mockMvc.perform(post("/recipes/create")
                        .param("name", "")) 
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/recipe-create"));
    }

    @Test
    void testShowEditForm() throws Exception {
        Long id = 1L;
        RecipeResponse mockResponse = new RecipeResponse();
        UpdateRecipeRequest mockRequest = new UpdateRecipeRequest();

        when(recipeService.getRecipeById(id)).thenReturn(Result.success("Ok", mockResponse));
        when(recipeMapper.toUpdateRequest(mockResponse)).thenReturn(mockRequest);

        mockMvc.perform(get("/recipes/{id}/update", id))
                .andExpect(status().isOk())
                .andExpect(view().name("recipe/recipe-edit"))
                .andExpect(model().attributeExists("recipeRequest"));
    }

    @Test
    void testUpdateRecipe_Success() throws Exception {
        Long id = 1L;
        
        mockMvc.perform(post("/recipes/{id}/update", id)
                        .param("name", "Updated Name")
                        .param("cookingTime", "20")
                        .param("difficulty", "EASY")
                        .param("imageUrl", "http://imagen.com/foto.jpg")
                        .param("ingredients[0].quantity", "1.0") 
                        .param("ingredients[0].ingredient.id", "1") 
                    )
                .andExpect(status().is3xxRedirection());
        
        verify(recipeService).updateRecipe(eq(id), any(UpdateRecipeRequest.class));
    }

    @Test
    void testDeleteRecipe() throws Exception {
        Long id = 1L;
        mockMvc.perform(post("/recipes/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/list"));
        
        verify(recipeService).deleteRecipe(id);
    }

    @Test
    void testRateRecipe_Success() throws Exception {
        Long recipeId = 1L;
        String username = "testuser";
        User mockUser = new User();
        mockUser.setId(100L);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/recipes/{id}/rate", recipeId)
                        .param("stars", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipeId + "/details"));

        verify(ratingService).rateRecipe(eq(100L), eq(recipeId), eq(5));
    }
}