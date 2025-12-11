package com.kume.kume.application.presentation.mappers;

import com.kume.kume.application.dto.RoleDto;
import com.kume.kume.infraestructure.models.Role;
import com.kume.kume.presentation.mappers.RoleMapper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {

    @Test
    void testToDto_Success() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN");

        RoleDto result = RoleMapper.toDto(role);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ROLE_ADMIN", result.getName());
    }

    @Test
    void testToDto_NullInput() {
        assertNull(RoleMapper.toDto(null));
    }

    @Test
    void testToEntity_Success() {
        RoleDto dto = RoleDto.builder()
                .id(2L)
                .name("ROLE_USER")
                .build();

        Role result = RoleMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("ROLE_USER", result.getName());
    }

    @Test
    void testToEntity_NullInput() {
        assertNull(RoleMapper.toEntity(null));
    }

    @Test
    void testConstructor() {
        RoleMapper mapper = new RoleMapper();
        assertNotNull(mapper);
    }
}