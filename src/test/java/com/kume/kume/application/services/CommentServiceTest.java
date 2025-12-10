package com.kume.kume.application.services;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kume.kume.application.dto.comment.CommentResponse;
import com.kume.kume.infraestructure.models.Comment;
import com.kume.kume.infraestructure.models.CommentLike;
import com.kume.kume.infraestructure.models.Recipe;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.CommentLikeRepository;
import com.kume.kume.infraestructure.repositories.CommentRepository;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.infraestructure.repositories.UserRepository;
import com.kume.kume.presentation.mappers.CommentMapper;


@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    // ------------------------------------------------------------------------------------
    // createComment
    // ------------------------------------------------------------------------------------

    @Test
    void createComment_ShouldCreateRootComment() {
        Long recipeId = 1L;
        Long userId = 10L;

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);

        User user = new User();
        user.setId(userId);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Comment saved = new Comment();
        saved.setId(100L);
        saved.setRecipe(recipe);
        saved.setUser(user);
        saved.setContent("Hola");

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        Comment result = commentService.createComment(recipeId, userId, "Hola", null);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WithParent_ShouldLinkParentComment() {
        Long recipeId = 1L;
        Long userId = 10L;
        Long parentId = 99L;

        Recipe recipe = new Recipe();
        recipe.setId(recipeId);

        User user = new User();
        user.setId(userId);

        Comment parent = new Comment();
        parent.setId(parentId);

        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment created = commentService.createComment(recipeId, userId, "Respuesta", parentId);

        assertNotNull(created.getParent());
        assertEquals(parentId, created.getParent().getId());
    }

    // ------------------------------------------------------------------------------------
    // editComment
    // ------------------------------------------------------------------------------------

    @Test
    void editComment_AsOwner_ShouldEditSuccessfully() {
        Long commentId = 1L;
        Long userId = 5L;

        Comment comment = new Comment();
        comment.setId(commentId);

        User owner = new User();
        owner.setId(userId);
        comment.setUser(owner);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = commentService.editComment(commentId, userId, "Nuevo contenido", false);

        assertEquals("Nuevo contenido", result.getContent());
        verify(commentRepository).save(comment);
    }

    @Test
    void editComment_NotOwnerAndNotAdmin_ShouldThrowException() {
        Long commentId = 1L;

        Comment comment = new Comment();
        comment.setId(commentId);

        User owner = new User();
        owner.setId(5L);
        comment.setUser(owner);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(RuntimeException.class, () ->
                commentService.editComment(commentId, 99L, "No permitido", false)
        );
    }

    // ------------------------------------------------------------------------------------
    // hideComment
    // ------------------------------------------------------------------------------------

    @Test
    void hideComment_ShouldSetHiddenToTrue() {
        Long id = 1L;

        Comment c = new Comment();
        c.setId(id);

        when(commentRepository.findById(id)).thenReturn(Optional.of(c));

        commentService.hideComment(id);

        assertTrue(c.isHidden());
        verify(commentRepository).save(c);
    }

    // ------------------------------------------------------------------------------------
    // rateComment
    // ------------------------------------------------------------------------------------

    @Test
    void rateComment_WhenLikeExists_ShouldUpdateIt() {
        Long commentId = 1L;
        Long userId = 5L;

        CommentLike existing = new CommentLike();
        existing.setId(50L);
        existing.setPositive(false);

        when(commentLikeRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Optional.of(existing));

        commentService.rateComment(commentId, userId, true);

        assertTrue(existing.getPositive());
        verify(commentLikeRepository).save(existing);
    }

    @Test
    void rateComment_WhenLikeDoesNotExist_ShouldCreateNew() {
        Long commentId = 1L;
        Long userId = 5L;

        Comment comment = new Comment();
        comment.setId(commentId);

        User user = new User();
        user.setId(userId);

        when(commentLikeRepository.findByCommentIdAndUserId(commentId, userId))
                .thenReturn(Optional.empty());
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        commentService.rateComment(commentId, userId, true);

        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    // ------------------------------------------------------------------------------------
    // getCommentById
    // ------------------------------------------------------------------------------------

    @Test
    void getCommentById_ShouldReturnComment() {
        Long id = 1L;
        Comment comment = new Comment();
        comment.setId(id);

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void getCommentById_NotFound_ShouldThrow() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                commentService.getCommentById(1L)
        );
    }

    // ------------------------------------------------------------------------------------
    // getRootComments
    // ------------------------------------------------------------------------------------

    @Test
    void getRootComments_ShouldReturnDtoList() {
        Long recipeId = 1L;

        Comment c1 = new Comment();
        c1.setId(10L);

        List<Comment> list = List.of(c1);

        CommentResponse dto = new CommentResponse();
        dto.setId(10L);

        when(commentRepository.findByRecipeIdAndParentIsNullOrderByCreatedAtDesc(recipeId))
                .thenReturn(list);

        when(commentMapper.toDtoList(list)).thenReturn(List.of(dto));

        List<CommentResponse> result = commentService.getRootComments(recipeId);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }
}