package com.kume.kume.presentation.controllers;

import com.kume.kume.application.dto.RecipeMediaDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.services.RecipeMediaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/recipe-media")
@AllArgsConstructor
public class RecipeMediaController {
    private final Path uploadDir = Paths.get("uploads");

    @Autowired
    private final RecipeMediaService recipeMediaService;

    @GetMapping("/list")
    public String listAll(Model model) {
        List<RecipeMediaDTO> media = recipeMediaService.getAll().getData();
        model.addAttribute("media", media);
        return "media/media-list";
    }

    @GetMapping("/recipe/{recipeId}")
    public String listByRecipe(@PathVariable("recipeId") Long recipeId, Model model) {
        List<RecipeMediaDTO> media = recipeMediaService.getByRecipeId(recipeId).getData();

        model.addAttribute("media", media);
        model.addAttribute("recipeId", recipeId);

        return "media/media-list-by-recipe";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("mediaRequest", new RecipeMediaDTO());
        return "media/media-create";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("mediaRequest") RecipeMediaDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "media/media-create";
        }

        recipeMediaService.create(request);
        redirectAttributes.addFlashAttribute("successMessage", "Media registrada exitosamente.");

        return "redirect:/recipe-media/list";
    }

    @GetMapping("/{id}/update")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Result<RecipeMediaDTO> media = recipeMediaService.getById(id);

        if (!media.isSuccess()) {
            return "redirect:/recipe-media/list?error=notfound";
        }

        model.addAttribute("mediaRequest", media.getData());
        return "media/media-edit";
    }

    @PostMapping("/{id}/update")
    public String update(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("mediaRequest") RecipeMediaDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "media/media-edit";
        }

        recipeMediaService.update(id, request);
        redirectAttributes.addFlashAttribute("successMessage", "Media actualizada exitosamente.");

        return "redirect:/recipe-media/list";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        recipeMediaService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Media eliminada exitosamente.");

        return "redirect:/recipe-media/list";
    }

    @GetMapping("/media/**")
    @ResponseBody
    public ResponseEntity<Resource> getImage(HttpServletRequest request) throws IOException {

        String requestedPath = request.getRequestURI().replace("/media/", "");
        Path file = uploadDir.resolve(requestedPath).normalize();

        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .contentType(
                        Files.probeContentType(file) == null
                                ? MediaType.APPLICATION_OCTET_STREAM
                                : MediaType.parseMediaType(Files.probeContentType(file)))
                .body(resource);
    }
}