package com.kume.kume.presentation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.kume.kume.application.dto.IngredientDTO;
import com.kume.kume.application.dto.Result;
import com.kume.kume.application.services.IngredientService;

@Controller
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public String index(@RequestParam(value = "edit", required = false) Long id, Model model) {
        if (id != null) {
            Result<?> resultIngredient = ingredientService.getById(id);
            model.addAttribute("ingredient", resultIngredient.getData());
        } else {
            model.addAttribute("ingredient", new IngredientDTO());
        }
        Result<?> resultList = ingredientService.getAll();
        model.addAttribute("result", resultList);
        return "ingredient/index";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute IngredientDTO dto) {
        if (dto.getId() == null) {
            ingredientService.create(dto);
        } else {
            ingredientService.update(dto.getId(), dto);
        }
        return "redirect:/ingredients";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        ingredientService.delete(id);
        return "redirect:/ingredients";
    }
}
