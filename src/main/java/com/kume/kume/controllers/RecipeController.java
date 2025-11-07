package com.kume.kume.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecipeController {
    /**
     * Mapea la URL privada para listar/visualizar recetas.
     * Solo accesible si el usuario está autenticado.
     */
    @GetMapping("/recipes")
    public String privateRecipesList() {
        return "recipe-list";
    }
}
