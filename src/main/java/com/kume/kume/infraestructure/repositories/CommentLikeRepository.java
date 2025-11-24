package com.kume.kume.infraestructure.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kume.kume.infraestructure.models.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);
    Integer countByCommentIdAndPositive(Long commentId, Boolean positive);
}
