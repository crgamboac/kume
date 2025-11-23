package com.kume.kume.presentation.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kume.kume.application.dto.comment.CommentResponse;
import com.kume.kume.infraestructure.models.Comment;
import com.kume.kume.infraestructure.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    private final UserRepository userRepository; // solo si necesitas datos adicionales (probablemente no)
    public CommentResponse toDto(Comment entity) {
        if (entity == null) return null;
        return CommentResponse.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .authorName(entity.getUser() != null ? entity.getUser().getFullName() : "Usuario desconocido")
                .createdAt(entity.getCreatedAt())
                .replies(
                        entity.getReplies() != null
                                ? entity.getReplies().stream()
                                      .map(this::toDto)
                                      .toList()
                                : List.of()
                )
                .build();
    }

    public List<CommentResponse> toDtoList(List<Comment> entities) {
        return entities == null
                ? List.of()
                : entities.stream().map(this::toDto).toList();
    }
}

