package com.kume.kume.application.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

import com.kume.kume.application.dto.Result;
import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.services.RecipeService;
import com.kume.kume.presentation.controllers.HomeController;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @InjectMocks
    private HomeController homeController;

    @Mock
    private RecipeService recipeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    void testHome_ReturnsViewAndModel() throws Exception {

        // Crear lista de recetas usando setters
        RecipeResponse r1 = new RecipeResponse();
        r1.setId(1L);
        r1.setName("Receta 1");

        RecipeResponse r2 = new RecipeResponse();
        r2.setId(2L);
        r2.setName("Receta 2");

        List<RecipeResponse> recipeList = List.of(r1, r2);

        // La clase result que usas aquí debe ser la del proyecto
        Result<List<RecipeResponse>> result = Result.success("OK", recipeList);

        when(recipeService.getAllRecipes()).thenReturn(result);

        // Ejecutar petición
        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/home"))
                .andExpect(model().attributeExists("recentRecipes"))
                .andExpect(model().attributeExists("popularRecipes"))
                .andExpect(model().attributeExists("commercialBanners"))
                .andReturn();

        // Validar modelo
        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

        List<RecipeResponse> recent = (List<RecipeResponse>) modelMap.get("recentRecipes");
        List<RecipeResponse> popular = (List<RecipeResponse>) modelMap.get("popularRecipes");
        List<?> banners = (List<?>) modelMap.get("commercialBanners");

        assertEquals(2, recent.size());
        assertEquals("Receta 1", recent.get(0).getName());

        assertEquals(2, popular.size()); // reversed()

        assertEquals(3, banners.size()); // loadDummyBanners(3)

        verify(recipeService).getAllRecipes();
    }
}