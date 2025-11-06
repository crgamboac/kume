package com.kume.kume.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kume.kume.config.JwtTokenUtil;
import com.kume.kume.dto.ChangePasswordRequest;
import com.kume.kume.dto.GenericResponse;
import com.kume.kume.models.User;
import com.kume.kume.repositories.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private UserRepository userRepository;
    private JwtTokenUtil jwtTokenUtil;
    
    public AuthController(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // LOGIN
    @GetMapping("/login")
    public ResponseEntity<GenericResponse<String>> login(@RequestParam String email, @RequestParam String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(GenericResponse.failure("Correo no registrado"));
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            return ResponseEntity.badRequest()
                    .body(GenericResponse.failure("Contraseña incorrecta"));
        }
        // token JWT
        String token = jwtTokenUtil.generateToken(user.getEmail(), user.getRole().getName());
        return ResponseEntity.ok(GenericResponse.success("Inicio de sesión exitoso", token));
    }

    // CAMBIAR CONTRASEÑA
    @PostMapping("/change-password/{id}")
    public ResponseEntity<GenericResponse<Void>> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(GenericResponse.failure("Usuario no encontrado"));
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(GenericResponse.failure("La contraseña actual no coincide"));
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        return ResponseEntity.ok(GenericResponse.success("Contraseña actualizada correctamente", null));
    }
}
