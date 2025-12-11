package com.kume.kume.configuration.security;

import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_Success() {
        String email = "test@kume.com";
        User mockUser = new User();
        mockUser.setUsername(email);
        mockUser.setPassword("securePass");

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(mockUser));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals(email, result.getUsername());
        assertEquals("securePass", result.getPassword());
        
        verify(userRepository).findByUsername(email);
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        String email = "fantasma@kume.com";
        
        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });

        verify(userRepository).findByUsername(email);
    }
}