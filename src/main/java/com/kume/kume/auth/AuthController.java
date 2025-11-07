package com.kume.kume.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kume.kume.dto.user.CreateUserRequest;
import com.kume.kume.services.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    /**
     * Mapea la URL GET /register para mostrar el formulario de registro.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userForm", new CreateUserRequest()); 
        return "register";
    }

    /**
     * Mapea la URL POST /register para procesar el envío del formulario.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userForm") @Valid CreateUserRequest user,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes) {

        if (!user.getPassword().equals(confirmPassword)) {
             redirectAttributes.addFlashAttribute("registrationError", "Las contraseñas no coinciden.");
             return "redirect:/register";
        }
        
        try {
            userService.register(user);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("registrationError", e.getMessage());
            return "redirect:/auth/register";
        }

        return "redirect:/auth/login?success";
    }
}
