package com.kume.kume.presentation.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kume.kume.application.dto.recipe.RecipeResponse;
import com.kume.kume.application.services.RecipeService;

import lombok.AllArgsConstructor;


@Controller
@RequestMapping("/")
@AllArgsConstructor
public class HomeController {

    @Autowired
    private final RecipeService recipeService;

    /**
     * Muestra la página de inicio con datos de recetas y banners.
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        
        // 1. Obtener Recetas
        List<RecipeResponse> recipes = recipeService.getAllRecipes().getData();
        
        // 2. Obtener Banners (Simulación)
        List<CommercialBanner> banners = loadDummyBanners(3);

        // 3. Agregar datos al modelo para Thymeleaf
        model.addAttribute("recentRecipes", recipes);
        model.addAttribute("popularRecipes", recipes.reversed());
        model.addAttribute("commercialBanners", banners);
        
        return "home/home";
    }

    // --- Clases de Modelo Simples (Usar Records en Java) ---
    
    private record CommercialBanner(String title, String imageUrl, String linkUrl) {}

    // --- Simulación de Datos ---

    private List<CommercialBanner> loadDummyBanners(int count) {
        return List.of(
            new CommercialBanner("¡La navidad llegó a Falabella!", "https://images.falabella.com/v3/assets/blt7c5c2f2f888a7cc3/blt7ebe2724231bc242/68f13ce0831bbe04a53e8ac3/Fan-deco-v01-dk-1231025OK-ja.gif?auto=webp&quality=70&width=90p", "https://www.falabella.com/falabella-cl/page/decoracion-navidad?sid=HO_BA_NAV_1909"),
            new CommercialBanner("¡Fechas dobles en Mercado Libre!", "https://http2.mlstatic.com/D_NQ_876444-MLA96652677894_112025-OO.webp", "https://click1.mercadolibre.cl/display/clicks/MLC/count?a=h%2FNMEAMfgd3TQr0ulIM3kcYc5CycA6dVcmFqKnuER8wP7nbH4iLdV966bgX63sIIJEX7qwTyah3D4c6V%2F1kpSXR1w496cXbiFGcQQ9kNSJ31TfE%2BgSoLBoA%2F78%2Bg0GSEvhgRsz%2FxykycL7pkw4Twbb3xLPPBSiBy8g6aC40eihuckYtzCbu1Cy%2Fzs25fC3WO%2BVKsRw0laEDyGljjHNePEOI5AQxN5lwAh1W3xj8dBrHyfLNj6tq0IDct9I%2FdY0%2FwszINLvwoErWlFqqOU%2BEYewHun3sJf8vltvNPrA3yTGeJn5nwvBGvPO0zeqEdAwrrT5gQ6knWStdfUn%2FuWMME7VwiCQdw7Pp8ZXaf0UhSYws0IQijwpXx5iIrBo0Z5s1dlKdCPdRBBlndHch%2BrkipLKAfTsf1t%2ByMr6abnPs6E%2FnWxuh2DmbKor9Id4h0nbIUyvdQGAT2Y7EcXsV7l72cNil2wlKPRm5ar8XCgQMO6hJbVg8hhXcwxYbC%2FmQhBxjyUJ%2FC0NSq%2BukmK0rqwKgt1Os3y%2F4OV5Aqa7E5lWvTBU%2F7GeEyuLO%2BU1mmUm%2F2mGSkXwygosQ0VLL85eelhsaTCzOJ%2BWgglsKqqRXE8FxNTRGu40MKpuXpHCzeUr2lwMZvHFDYamy51JqYbFDOixnoDnOB0JYWcVgpFVQBplsuaDliXv5Z4%2Bmlf9lzX1MU8iK%2FzcWKx%2FTo0468nZGtCUKDVmLJhcKmUrEdTvL3ggVeszE8WgFzLU%2F4OOanlw58Go5atgUaheyjSK1IDHwVsrX0H7T3UoaLUd4%3D#c_id=/home/exhibitors-carousel/element&c_campaign=display%2Fclicks%2FMLC%2Fcount&c_element_order=2&c_uid=461ee545-e5c1-4aca-aebf-d435737e85bb"),
            new CommercialBanner("¡Los juguetes llegaron a Ripley!", "https://simple.ripley.cl/home/_next/image?url=https%3A%2F%2Fapi.ripley.com%2Fexperience%2Fecommerce%2Frdex%2Fapi-image-interceptor%2Fv1%2Fimages%2FaQ6Vf7pReVYa4Qm5_sl-desk-toysrus-071125-cl.webp&w=3840&q=100", "https://simple.ripley.cl/landings/toysrus")
        );
    }
}
