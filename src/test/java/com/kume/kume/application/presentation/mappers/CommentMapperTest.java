package com.kume.kume.application.presentation.mappers;

import com.kume.kume.application.dto.comment.CommentResponse;
import com.kume.kume.infraestructure.models.Comment;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.UserRepository;
import com.kume.kume.presentation.mappers.CommentMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Mock
    private UserRepository userRepository;

    private CommentMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Instanciamos el mapper inyectando el mock del repositorio
        mapper = new CommentMapper(userRepository);
    }

    @Test
    void testToDto_FullData() {
        // 1. Given (Preparamos datos completos: Usuario + Respuestas)
        User user = new User();
        user.setFullName("Juan Perez");

        Comment reply = new Comment();
        reply.setId(2L);
        reply.setContent("Soy una respuesta");
        reply.setUser(user); // La respuesta también tiene usuario

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Comentario principal");
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setReplies(List.of(reply)); // Tiene respuestas

        // 2. When
        CommentResponse dto = mapper.toDto(comment);

        // 3. Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Comentario principal", dto.getContent());
        assertEquals("Juan Perez", dto.getAuthorName());
        assertEquals(1, dto.getReplies().size());
        
        // Verificamos que la respuesta anidada también se mapeó (Recursividad)
        assertEquals("Soy una respuesta", dto.getReplies().get(0).getContent());
    }

    @Test
    void testToDto_NullUserAndNullReplies() {
        // 1. Given (Comentario SIN usuario y SIN lista de respuestas)
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("Comentario anónimo");
        comment.setUser(null);     // Probamos la rama "Usuario desconocido"
        comment.setReplies(null);  // Probamos la rama de lista vacía

        // 2. When
        CommentResponse dto = mapper.toDto(comment);

        // 3. Then
        assertNotNull(dto);
        assertEquals("Usuario desconocido", dto.getAuthorName()); // Verifica lógica ternaria
        assertNotNull(dto.getReplies()); // No debe ser null
        assertTrue(dto.getReplies().isEmpty()); // Debe ser lista vacía
    }

    @Test
    void testToDto_NullInput() {
        // Probamos if (entity == null) return null;
        assertNull(mapper.toDto(null));
    }

    @Test
    void testToDtoList_Success() {
        // 1. Given
        Comment comment = new Comment();
        comment.setContent("Test");
        List<Comment> entities = List.of(comment);

        // 2. When
        List<CommentResponse> dtos = mapper.toDtoList(entities);

        // 3. Then
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals("Test", dtos.get(0).getContent());
    }

    @Test
    void testToDtoList_NullInput() {
        // Probamos que retorne lista vacía si la entrada es null
        List<CommentResponse> dtos = mapper.toDtoList(null);
        
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}