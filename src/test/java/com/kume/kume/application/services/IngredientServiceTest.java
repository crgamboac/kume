package com.kume.kume.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kume.kume.application.dto.IngredientDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.infraestructure.models.Ingredient;
import com.kume.kume.infraestructure.models.IngredientType;
import com.kume.kume.infraestructure.repositories.IngredientRepository;

@ExtendWith(MockitoExtension.class)
public class IngredientServiceTest {
    private IngredientRepository ingredientRepository;
    private IngredientService ingredientService;

    @BeforeEach
    void setUp() {
        ingredientRepository = mock(IngredientRepository.class);
        ingredientService = new IngredientService(ingredientRepository);
    }

    // ---------------------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------------------
    @Test
    void testGetAll_ReturnsList() {
        Ingredient ing1 = new Ingredient(1L, "Tomate", IngredientType.VEGETABLE, null);
        Ingredient ing2 = new Ingredient(2L, "Pollo", IngredientType.VEGETABLE, null);

        when(ingredientRepository.findAll()).thenReturn(Arrays.asList(ing1, ing2));

        Result<List<IngredientDTO>> result = ingredientService.getAll();

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
        verify(ingredientRepository, times(1)).findAll();
    }

    // ---------------------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------------------
    @Test
    void testGetById_Found() {
        Ingredient ing = new Ingredient(1L, "Queso", IngredientType.VEGETABLE, null);
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ing));

        Result<IngredientDTO> result = ingredientService.getById(1L);

        assertTrue(result.isSuccess());
        assertEquals("Queso", result.getData().getName());
        verify(ingredientRepository, times(1)).findById(1L);
    }

    @Test
    void testGetById_NotFound() {
        when(ingredientRepository.findById(99L)).thenReturn(Optional.empty());

        Result<IngredientDTO> result = ingredientService.getById(99L);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        verify(ingredientRepository).findById(99L);
    }

    // ---------------------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------------------
    @Test
    void testCreate_Success() {
        IngredientDTO dto = new IngredientDTO( null,"Sal", IngredientType.VEGETABLE);
        Ingredient entity = dto.toEntity();

        // No es necesario retornar algo concreto, solo verificar que se llama
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(entity);

        Result<IngredientDTO> result = ingredientService.create(dto);

        assertTrue(result.isSuccess());
        assertEquals("Sal", result.getData().getName());

        // Verificar captura del argumento guardado
        ArgumentCaptor<Ingredient> captor = ArgumentCaptor.forClass(Ingredient.class);
        verify(ingredientRepository).save(captor.capture());

        assertEquals("Sal", captor.getValue().getName());
    }

    // ---------------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------------
    @Test
    void testUpdate_Found() {
        Ingredient existing = new Ingredient(1L, "Harina", IngredientType.VEGETABLE, null);
        IngredientDTO dto = new IngredientDTO(1L, "Harina Integral", IngredientType.VEGETABLE);

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ingredientRepository.save(existing)).thenReturn(existing);

        Result<IngredientDTO> result = ingredientService.update(1L, dto);

        assertTrue(result.isSuccess());
        assertEquals("Harina Integral", result.getData().getName());
        verify(ingredientRepository).save(existing);
    }

    @Test
    void testUpdate_NotFound() {
        IngredientDTO dto = new IngredientDTO(1L, "Algo", IngredientType.VEGETABLE);

        when(ingredientRepository.findById(99L)).thenReturn(Optional.empty());

        Result<IngredientDTO> result = ingredientService.update(99L, dto);

        assertFalse(result.isSuccess());
        verify(ingredientRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------------
    @Test
    void testDelete_Found() {
        Ingredient ing = new Ingredient(3L, "Aceite", IngredientType.VEGETABLE, null);

        when(ingredientRepository.findById(3L)).thenReturn(Optional.of(ing));

        Result<Void> result = ingredientService.delete(3L);

        assertTrue(result.isSuccess());
        verify(ingredientRepository).delete(ing);
    }

    @Test
    void testDelete_NotFound() {
        when(ingredientRepository.findById(100L)).thenReturn(Optional.empty());

        Result<Void> result = ingredientService.delete(100L);

        assertFalse(result.isSuccess());
        verify(ingredientRepository, never()).delete(any());
    }
}
