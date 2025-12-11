package com.kume.kume.application.controllers;

import com.kume.kume.application.dto.IngredientDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.services.IngredientService;
import com.kume.kume.presentation.controllers.IngredientController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IngredientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IngredientService ingredientService;

    @InjectMocks
    private IngredientController ingredientController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ingredientController).build();
    }

    // ---------------------------------------------------------
    // GET /ingredient (SIN EDIT)
    // ---------------------------------------------------------
    @Test
    void testIndexWithoutEdit() throws Exception {

        Result<List<IngredientDTO>> ingredientList =
        new Result<>(true, null, Collections.emptyList());
        when(ingredientService.getAll()).thenReturn(ingredientList);

        mockMvc.perform(get("/ingredient"))
                .andExpect(status().isOk())
                .andExpect(view().name("ingredient/index"))
                .andExpect(model().attributeExists("ingredient"))
                .andExpect(model().attributeExists("result"));

        verify(ingredientService).getAll();
    }

    // ---------------------------------------------------------
    // GET /ingredient?edit=5
    // ---------------------------------------------------------
    @Test
    void testIndexWithEdit() throws Exception {

        IngredientDTO dto = new IngredientDTO();
        dto.setId(5L);
        dto.setName("Azúcar");

        // Respeta el orden del constructor: (success, message, data)
        Result<IngredientDTO> ingredient = new Result<>(true, null, dto);
        Result<List<IngredientDTO>> ingredientList = new Result<>(true, null, Arrays.asList(dto));

        when(ingredientService.getById(5L)).thenReturn(ingredient);
        when(ingredientService.getAll()).thenReturn(ingredientList);

        mockMvc.perform(get("/ingredient").param("edit", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("ingredient/index"))
                .andExpect(model().attributeExists("ingredient"))
                .andExpect(model().attributeExists("result"));

        verify(ingredientService).getById(5L);
        verify(ingredientService).getAll();
    }


    // ---------------------------------------------------------
    // GET /ingredient/edit/{id}
    // ---------------------------------------------------------
    @Test
    void testEditIngredient() throws Exception {

        IngredientDTO dto = new IngredientDTO();
        dto.setId(3L);
        dto.setName("Sal");

        Result<IngredientDTO> ingredient = new Result<>(true, null, dto);
        when(ingredientService.getById(3L)).thenReturn(ingredient);

        mockMvc.perform(get("/ingredient/edit/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("ingredient/index"))
                .andExpect(model().attributeExists("ingredient"));

        verify(ingredientService).getById(3L);
    }


    // ---------------------------------------------------------
    // POST /ingredient/save — CREATE
    // ---------------------------------------------------------
   @Test
    void testCreateIngredient() throws Exception {

        IngredientDTO dto = new IngredientDTO();
        dto.setName("Azúcar");

        Result<IngredientDTO> result = new Result<>(true, null, dto);

        when(ingredientService.create(any(IngredientDTO.class))).thenReturn(result);

        mockMvc.perform(post("/ingredient/save")
                .param("name", "Azúcar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ingredient"));

        verify(ingredientService).create(any(IngredientDTO.class));
    }

   
    // ---------------------------------------------------------
    // POST /ingredient/save — UPDATE
    // ---------------------------------------------------------
    @Test
    void testUpdateIngredient() throws Exception {

        IngredientDTO dto = new IngredientDTO();
        dto.setId(10L);
        dto.setName("Aceite");

        Result<IngredientDTO> result = new Result<>(true, null, dto);

        when(ingredientService.update(eq(10L), any(IngredientDTO.class)))
                .thenReturn(result);

        mockMvc.perform(post("/ingredient/save")
                        .param("id", "10")
                        .param("name", "Aceite"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ingredient"));

        verify(ingredientService).update(eq(10L), any(IngredientDTO.class));
    }

    @Test
    void testDeleteIngredient() throws Exception {

        Result<Void> result = new Result<>(true, null, null);

        when(ingredientService.delete(6L)).thenReturn(result);

        mockMvc.perform(get("/ingredient/delete/6"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ingredient"));

        verify(ingredientService).delete(6L);
    }

   
}

