package com.kume.kume.application.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kume.kume.application.dto.comment.CommentResponse;
import com.kume.kume.infraestructure.models.Comment;
import com.kume.kume.infraestructure.models.CommentLike;
import com.kume.kume.infraestructure.repositories.CommentLikeRepository;
import com.kume.kume.infraestructure.repositories.CommentRepository;
import com.kume.kume.infraestructure.repositories.RecipeRepository;
import com.kume.kume.infraestructure.repositories.UserRepository;
import com.kume.kume.presentation.mappers.CommentMapper;

@Service
public class CommentService {
        private RecipeRepository recipeRepository;
        private UserRepository userRepository;
        private CommentRepository commentRepository;
        private CommentLikeRepository commentLikeRepository;
        private final CommentMapper commentMapper;
        public CommentService(RecipeRepository recipeRepository, UserRepository userRepository,
                CommentRepository commentRepository, CommentMapper commentMapper) {
            this.recipeRepository = recipeRepository;
            this.userRepository = userRepository;
            this.commentRepository = commentRepository;
            this.commentMapper = commentMapper;
        }

        public Comment createComment(Long recipeId, Long userId, String content, Long parentId) {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setRecipe(recipeRepository.findById(recipeId).orElseThrow());
            comment.setUser(userRepository.findById(userId).orElseThrow());
            comment.setParent(parentId != null ? commentRepository.findById(parentId).orElseThrow() : null);
            comment.setCreatedAt(LocalDateTime.now());
            return commentRepository.save(comment);
        }

        public Comment editComment(Long commentId, Long userId, String content, boolean isAdmin) {
            Comment comment = commentRepository.findById(commentId).orElseThrow();

            if (!isAdmin && !comment.getUser().getId().equals(userId)) {
                throw new RuntimeException("No puedes editar este comentario.");
            }

            comment.setContent(content);
            comment.setUpdatedAt(LocalDateTime.now());
            return commentRepository.save(comment);
        }

        public void hideComment(Long commentId) {
            Comment comment = commentRepository.findById(commentId).orElseThrow();
            comment.setHidden(true);
            commentRepository.save(comment);
        }

        public void rateComment(Long commentId, Long userId, boolean positive) {
            Optional<CommentLike> existing = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

            if (existing.isPresent()) {
                CommentLike like = existing.get();
                like.setPositive(positive);
                commentLikeRepository.save(like);
                return;
            }

            CommentLike like = new CommentLike();
            like.setComment(commentRepository.findById(commentId).orElseThrow());
            like.setUser(userRepository.findById(userId).orElseThrow());
            like.setPositive(positive);
            commentLikeRepository.save(like);
        }

        public Comment getCommentById(Long id) {
            return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        }

        // public List<Comment> getRootComments(Long recipeId) {
        //     return commentRepository.findByRecipeIdAndParentIsNullOrderByCreatedAtDesc(recipeId);
        // }      
        
        public List<CommentResponse> getRootComments(Long recipeId) {
            List<Comment> rootComments =
                    commentRepository.findByRecipeIdAndParentIsNullOrderByCreatedAtDesc(recipeId);

            return commentMapper.toDtoList(rootComments);
        }
}
