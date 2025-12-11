package com.kume.kume.presentation.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kume.kume.application.services.CommentService;
import com.kume.kume.infraestructure.models.Comment;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/recipes/{recipeId}/comments")
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public String createComment(
            @PathVariable Long recipeId,
            @RequestParam Long userId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId
    ) {
        commentService.createComment(recipeId, userId, content, parentId);
        return "redirect:/recipes/" + recipeId + "/details";
    }

    @GetMapping("/{commentId}/edit")
    public String editForm(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            Model model
    ) {
        Comment comment = commentService.getCommentById(commentId);
        model.addAttribute("comment", comment);
        model.addAttribute("recipeId", recipeId);

        return "comment/edit";  // thymeleaf template
    }

    @PostMapping("/{commentId}/edit")
    public String edit(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestParam String content
    ) {
        commentService.editComment(commentId, userId, content, false);
        return "redirect:/recipes/" + recipeId;
    }

    @PostMapping("/{commentId}/hide")
    public String hide(
            @PathVariable Long recipeId,
            @PathVariable Long commentId
    ) {
        commentService.hideComment(commentId);
        return "redirect:/recipes/" + recipeId;
    }

    @PostMapping("/{commentId}/like-dislike")
    public String like(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestParam Boolean userOption
    ) {
        commentService.rateComment(commentId, userId, userOption);
        return "redirect:/recipes/" + recipeId;
    }
}
