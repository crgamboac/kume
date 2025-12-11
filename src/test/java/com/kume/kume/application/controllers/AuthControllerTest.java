package com.kume.kume.application.controllers;

import com.kume.kume.application.dto.user.CreateUserRequest;
import com.kume.kume.application.services.UserService;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.presentation.controllers.auth.AuthController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // ---------------------------------------------------------
    // GET /auth/login
    // ---------------------------------------------------------
    @Test
    void testLoginView() throws Exception {

        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    // ---------------------------------------------------------
    // GET /auth/register
    // ---------------------------------------------------------
    @Test
    void testShowRegistrationForm() throws Exception {

        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("userForm"));
    }

    // ---------------------------------------------------------
    // POST /auth/register — Contraseñas no coinciden
    // ---------------------------------------------------------
    @Test
    void testRegisterUser_PasswordMismatch() throws Exception {

        mockMvc.perform(post("/auth/register")
                .param("username", "juan")
                .param("email", "juan@example.com")
                .param("password", "1234")
                .param("confirmPassword", "9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))  // el controlador te redirige a /register
                .andExpect(flash().attributeExists("registrationError"));

        verify(userService, never()).register(any(CreateUserRequest.class));
    }

    // ---------------------------------------------------------
    // POST /auth/register — UserService lanza excepción
    // ---------------------------------------------------------
    @Test
    void testRegisterUser_ServiceException() throws Exception {

        doThrow(new IllegalStateException("Usuario ya existe"))
                .when(userService).register(any(CreateUserRequest.class));

        mockMvc.perform(post("/auth/register")
                .param("username", "juan")
                .param("email", "juan@example.com")
                .param("password", "1234")
                .param("confirmPassword", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/register"))
                .andExpect(flash().attributeExists("registrationError"));

        verify(userService).register(any(CreateUserRequest.class));
    }

    // ---------------------------------------------------------
    // POST /auth/register — Éxito
    // ---------------------------------------------------------
    @Test
    void testRegisterUser_Success() throws Exception {

        when(userService.register(any(CreateUserRequest.class)))
                .thenReturn(new User());

        mockMvc.perform(post("/auth/register")
                .param("username", "juan")
                .param("email", "juan@example.com")
                .param("password", "1234")
                .param("confirmPassword", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?success"));

        verify(userService).register(any(CreateUserRequest.class));
    }

}
