package com.kume.kume.application.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kume.kume.application.dto.user.CreateUserRequest;

import com.kume.kume.infraestructure.models.Role;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.RoleRepository;
import com.kume.kume.infraestructure.repositories.UserRepository;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;  // Se prueba esta clase

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==============================================================
    // TEST: Registro exitoso
    // ==============================================================
    @Test
    void testRegister_Success() {
        // Datos de entrada
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("juan");
        request.setFullName("juan@test.com");
        request.setPassword("1234");

        // Mocks
        when(userRepository.findByUsername("juan")).thenReturn(Optional.empty());

        Role userRole = new Role(1L, "ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("1234")).thenReturn("hashed1234");

        // Capturar el User final para validar
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        User storedUser = new User();
        storedUser.setId(100L);

        when(userRepository.save(any(User.class))).thenReturn(storedUser);

        // Ejecutar
        User result = userService.register(request);

        // Validar
        assertNotNull(result);
        assertEquals(100L, result.getId());

        // Verificar Save
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("juan", saved.getUsername());
        assertEquals("hashed1234", saved.getPassword());
        assertEquals(1, saved.getRoles().size());
        assertTrue(saved.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_USER")));
    }

    // ==============================================================
    // TEST: Registro falla por usuario existente
    // ==============================================================
    @Test
    void testRegister_Fails_UserAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("juan");

        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(new User()));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> userService.register(request)
        );

        assertEquals("El nombre de usuario/email ya está registrado.", ex.getMessage());

        verify(userRepository, never()).save(any());
    }

    // ==============================================================
    // TEST: Registro falla por rol faltante
    // ==============================================================
    @Test
    void testRegister_Fails_RoleNotFound() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("juan");
        request.setPassword("1234");

        when(userRepository.findByUsername("juan"))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> userService.register(request)
        );

        assertEquals("Rol 'ROLE_USER' no encontrado en la base de datos.", ex.getMessage());

        verify(userRepository, never()).save(any());
    }
}
