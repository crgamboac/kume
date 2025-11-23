package com.kume.kume.infraestructure.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kume.kume.infraestructure.models.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRecipeIdAndParentIsNullOrderByCreatedAtDesc(Long recipeId);
}
