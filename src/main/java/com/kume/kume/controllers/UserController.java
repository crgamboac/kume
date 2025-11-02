package com.kume.kume.controllers;
import com.kume.kume.dto.GenericResponse;
import com.kume.kume.dto.UserDto;
import com.kume.kume.mappers.RoleMapper;
import com.kume.kume.mappers.UserMapper;
import com.kume.kume.models.Role;
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
    public ResponseEntity<GenericResponse<List<UserDto>>> getAll() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok( GenericResponse.success( "Usuarios obtenidos correctamente", users.stream().map(UserMapper::toDto).toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<UserDto>> getById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(new GenericResponse<>(false, "Usuario no encontrado", null));
        }
        return ResponseEntity.ok( GenericResponse.success( "Usuario encontrado", UserMapper.toDto(user.get())));
    }

    @PostMapping
    public ResponseEntity<GenericResponse<UserDto>> create(@RequestBody UserDto user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body( GenericResponse.failure( "El correo ya está registrado."));
        }

        if (user.getRole() == null || user.getRole().getId() == null) {
            return ResponseEntity
                    .badRequest()
                    .body( GenericResponse.failure( "Debe especificar un rol válido."));
        }

        var role = roleRepository.findById(user.getRole().getId())
                .orElse(null);

        if (role == null) {
            return ResponseEntity
                    .badRequest()
                    .body( GenericResponse.failure( "Rol no encontrado."));
        }

        User newUser = UserMapper.toEntity(user);
        newUser.setRole(role);
        User saved = userRepository.save(newUser);
        return ResponseEntity
                .ok( GenericResponse.success( "Usuario creado exitosamente", UserMapper.toDto(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<UserDto>> update(@PathVariable Long id, @RequestBody UserDto updatedUser) {
        Optional<User> existing = userRepository.findById(id);

        if (existing.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(GenericResponse.failure( "Usuario no encontrado"));
        }

        User user = existing.get();
        user.setFull_name(updatedUser.getFull_name());
        user.setEmail(updatedUser.getEmail());
        user.setRole(RoleMapper.toEntity(updatedUser.getRole()));

        User saved = userRepository.save(user);
        return ResponseEntity.ok( GenericResponse.success("Usuario actualizado correctamente", UserMapper.toDto(saved)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity
                    .status(404)
                    .body( GenericResponse.failure("Usuario no encontrado"));
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(GenericResponse.success("Usuario eliminado correctamente", null));
    }

}
