package com.kume.kume.application.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kume.kume.application.dto.Result;
import com.kume.kume.application.services.CommentService;
import com.kume.kume.infraestructure.models.Comment;
import com.kume.kume.presentation.controllers.CommentController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    // ---------------------------------------------------------
    // TEST: CREATE COMMENT
    // ---------------------------------------------------------
    @Test
    void testCreateComment() throws Exception {
        Comment saved = new Comment();
        saved.setId(1L);
        saved.setContent("Texto comentario");

        when(commentService.createComment(10L, 5L, "Texto comentario", null))
                .thenReturn(saved);

        mockMvc.perform(post("/recipes/10/comments")
                        .param("userId", "5")
                        .param("content", "Texto comentario"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/10/details"));

        verify(commentService).createComment(10L, 5L, "Texto comentario", null);
    }

    // ---------------------------------------------------------
    // TEST: EDIT FORM
    // ---------------------------------------------------------
    @Test
    void testEditForm() throws Exception {

        Comment c = new Comment();
        c.setId(7L);
        c.setContent("Contenido viejo");

        when(commentService.getCommentById(7L)).thenReturn(c);

        mockMvc.perform(get("/recipes/4/comments/7/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/edit"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("recipeId", 4L));

        verify(commentService).getCommentById(7L);
    }

    // ---------------------------------------------------------
    // TEST: EDIT COMMENT (POST)
    // ---------------------------------------------------------
    @Test
    void testEditComment() throws Exception {

        Comment edited = new Comment();
        edited.setId(7L);
        edited.setContent("Nuevo contenido");

        when(commentService.editComment(7L, 5L, "Nuevo contenido", false))
                .thenReturn(edited);

        mockMvc.perform(post("/recipes/4/comments/7/edit")
                        .param("userId", "5")
                        .param("content", "Nuevo contenido"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/4"));

        verify(commentService).editComment(7L, 5L, "Nuevo contenido", false);
    }

    // ---------------------------------------------------------
    // TEST: HIDE COMMENT
    // ---------------------------------------------------------
    @Test
    void testHideComment() throws Exception {
        doNothing()
                .when(commentService)
                .hideComment(9L);

        mockMvc.perform(post("/recipes/3/comments/9/hide"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/3"));

        verify(commentService).hideComment(9L);
    }

    // ---------------------------------------------------------
    // TEST: LIKE / DISLIKE
    // ---------------------------------------------------------
    @Test
    void testRateComment() throws Exception {

        doNothing()
                .when(commentService)
                .rateComment(12L, 5L, true);

        mockMvc.perform(post("/recipes/8/comments/12/like-dislike")
                        .param("userId", "5")
                        .param("userOption", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/8"));

        verify(commentService).rateComment(12L, 5L, true);
    }


}
