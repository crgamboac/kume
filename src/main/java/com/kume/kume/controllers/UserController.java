package com.kume.kume.controllers;
import com.kume.kume.dto.GenericResponse;
import com.kume.kume.models.User;
import com.kume.kume.repositories.RoleRepository;
import com.kume.kume.repositories.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;   
    
    
    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    
    @GetMapping
    public ResponseEntity<GenericResponse<List<User>>> getAll() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(new GenericResponse<>(true, "Usuarios obtenidos correctamente", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<User>> getById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(new GenericResponse<>(false, "Usuario no encontrado", null));
        }
        return ResponseEntity.ok(new GenericResponse<>(true, "Usuario encontrado", user.get()));
    }

    @PostMapping
    public ResponseEntity<GenericResponse<User>> create(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new GenericResponse<>(false, "El correo ya está registrado.", null));
        }

        if (user.getRole() == null || user.getRole().getId() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new GenericResponse<>(false, "Debe especificar un rol válido.", null));
        }

        var role = roleRepository.findById(user.getRole().getId())
                .orElse(null);

        if (role == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new GenericResponse<>(false, "Rol no encontrado.", null));
        }

        user.setRole(role);
        User saved = userRepository.save(user);
        return ResponseEntity
                .ok(new GenericResponse<>(true, "Usuario creado exitosamente", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<User>> update(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> existing = userRepository.findById(id);

        if (existing.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(new GenericResponse<>(false, "Usuario no encontrado", null));
        }

        User user = existing.get();
        user.setFull_name(updatedUser.getFull_name());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(updatedUser.getPassword());
        user.setRole(updatedUser.getRole());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(new GenericResponse<>(true, "Usuario actualizado correctamente", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity
                    .status(404)
                    .body(new GenericResponse<>(false, "Usuario no encontrado", null));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(new GenericResponse<>(true, "Usuario eliminado correctamente", null));
    }

}
