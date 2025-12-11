package com.kume.kume.application.presentation.mappers;

import com.kume.kume.application.dto.user.CreateUserRequest;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.presentation.mappers.UserMapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void testToDto_Success() {
        User user = new User();
        user.setFullName("Cristopher Gamboa");
        user.setUsername("cgamboa");

        CreateUserRequest dto = mapper.toDto(user);

        assertNotNull(dto);
        assertEquals("Cristopher Gamboa", dto.getFullName());
        assertEquals("cgamboa", dto.getUsername());
    }

    @Test
    void testToDto_NullInput() {
        CreateUserRequest dto = mapper.toDto(null);
        
        assertNotNull(dto);
        assertNull(dto.getUsername()); // Debería estar vacío
    }

    @Test
    void testToEntity_Success() {
        CreateUserRequest request = CreateUserRequest.builder()
                .fullName("Sebastian")
                .username("seba123")
                .build();

        User entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Sebastian", entity.getFullName());
        assertEquals("seba123", entity.getUsername());
    }

    @Test
    void testToEntity_NullInput() {
        User entity = mapper.toEntity(null);

        assertNotNull(entity);
        assertNull(entity.getUsername()); // Debería estar vacío
    }
}
