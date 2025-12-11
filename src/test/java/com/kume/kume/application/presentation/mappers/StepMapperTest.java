package com.kume.kume.application.presentation.mappers;

import com.kume.kume.application.dto.recipe.CreateStepRequest;
import com.kume.kume.infraestructure.models.Step;
import com.kume.kume.presentation.mappers.StepMapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StepMapperTest {

    @Test
    void testToEntity_Success() {
        CreateStepRequest request = CreateStepRequest.builder()
                .stepNumber(1L)
                .instruction("Mezclar todo con fuerza")
                .build();

        Step result = StepMapper.toEntity(request);

        assertNotNull(result);
        assertEquals(1L, result.getStepNumber());
        assertEquals("Mezclar todo con fuerza", result.getInstruction());
    }

    @Test
    void testToEntity_NullRequest() {
        Step result = StepMapper.toEntity(null);
        
        assertNull(result);
    }

    @Test
    void testConstructor() {
        StepMapper mapper = new StepMapper();
        assertNotNull(mapper);
    }
}
