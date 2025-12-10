package com.kume.kume.application.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kume.kume.application.dto.user.CreateUserRequest;
import com.kume.kume.infraestructure.models.Role;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.RoleRepository;
import com.kume.kume.infraestructure.repositories.UserRepository;
import com.kume.kume.presentation.mappers.UserMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    // Inyectamos el servicio
    @InjectMocks
    private UserService userService;

    private CreateUserRequest validRequest;
    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        validRequest = new CreateUserRequest();
        validRequest.setUsername("testuser");
        validRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setUsername("testuser");

        mockRole = new Role();
        mockRole.setName("ROLE_USER");
    }

    // --- 1. Test de Registro Exitoso (CORREGIDO) ---
    @Test
    void register_NewUserAndRoleFound_ShouldReturnSavedUser() throws IllegalStateException {
        // ARRANGE
        // El objeto User que debería ser creado por el mapper estático.
        // Lo creamos aquí para que no sea null.
        User mappedUser = new User();
        mappedUser.setUsername(validRequest.getUsername());

        // Simulación del método estático UserMapper.toEntity()
        // Se usa try-with-resources para mockear métodos estáticos
        try (var mockedMapper = mockStatic(UserMapper.class)) {
            // 1. Usuario no existente
            when(userRepository.findByUsername(validRequest.getUsername())).thenReturn(Optional.empty());

            // 2. Simulación de PasswordEncoder
            String hashedPassword = "hashed_password";
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(hashedPassword);

            // 3. Rol encontrado
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(mockRole));

            // 4. Mapeo estático DEBE devolver un objeto no nulo
            mockedMapper.when(() -> userMapper.toEntity(validRequest)).thenReturn(mappedUser);

            // 5. Usuario guardado
            when(userRepository.save(any(User.class))).thenReturn(mappedUser);

            // ACT
            User registeredUser = userService.register(validRequest);

            // ASSERT
            assertNotNull(registeredUser);
            assertEquals("testuser", registeredUser.getUsername());
            assertEquals(hashedPassword, registeredUser.getPassword());
            assertEquals(1, registeredUser.getRoles().size());

            // Verificaciones de interacción
            verify(userRepository, times(1)).findByUsername(anyString());
            verify(passwordEncoder, times(1)).encode(anyString());
            verify(roleRepository, times(1)).findByName("ROLE_USER");
            verify(userRepository, times(1)).save(mappedUser);
        }
    }

    // --- 2. Test de Fallo por Usuario Existente ---
    @Test
    void register_ExistingUsername_ShouldThrowIllegalStateException() {
        // ARRANGE
        when(userRepository.findByUsername(validRequest.getUsername())).thenReturn(Optional.of(mockUser));

        // ACT & ASSERT
        IllegalStateException exception = assertThrows(IllegalStateException.class, ()
                -> userService.register(validRequest)
        );

        assertEquals("El nombre de usuario/email ya está registrado.", exception.getMessage());

        // Verificaciones de interacción
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- 3. Test de Fallo por Rol No Encontrado ---
    // Código de prueba corregido (solo el método afectado)
    @Test
    void register_UserNotFoundButRoleMissing_ShouldThrowIllegalStateException() {
        // ARRANGE
        // NOTA: Se elimina el bloque try-with-resources que contenía el stub de UserMapper.toEntity

        // 1. Usuario no existente
        when(userRepository.findByUsername(validRequest.getUsername())).thenReturn(Optional.empty());

        // 2. Simulación de PasswordEncoder
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashed_password");

        // 3. Rol NO encontrado
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // ACT & ASSERT
        IllegalStateException exception = assertThrows(IllegalStateException.class, ()
                -> userService.register(validRequest)
        );

        assertEquals("Rol 'ROLE_USER' no encontrado en la base de datos.", exception.getMessage());

        // Verificaciones de interacción
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(userRepository, never()).save(any(User.class));
    }
}
