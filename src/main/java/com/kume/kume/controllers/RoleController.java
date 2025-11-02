package com.kume.kume.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kume.kume.dto.GenericResponse;
import com.kume.kume.dto.RoleDto;
import com.kume.kume.mappers.RoleMapper;
import com.kume.kume.models.Role;
import com.kume.kume.repositories.RoleRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<GenericResponse<List<RoleDto>>> getAll() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(GenericResponse.success("Roles obtenidos correctamente",
                roles.stream().map(RoleMapper::toDto).toList()));
    }

    @PostMapping
    public ResponseEntity<GenericResponse<RoleDto>> create(@RequestBody RoleDto role) {
        if (roleRepository.existsByName(role.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body(GenericResponse.failure("El rol '" + role.getName() + "' ya existe"));
        }

        Role savedRole = roleRepository.save(RoleMapper.toEntity(role));
        return ResponseEntity.ok(GenericResponse.success("Rol creado correctamente", RoleMapper.toDto(savedRole)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<Void>> delete(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity
                    .badRequest()
                    .body(GenericResponse.failure("El rol no existe"));
        }

        roleRepository.deleteById(id);
        return ResponseEntity.ok(GenericResponse.success("Rol eliminado correctamente", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<RoleDto>> getById(@PathVariable Long id) {
        Optional<Role> role = roleRepository.findById(id);
        if (role.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body( GenericResponse.failure( "El rol con ID " + id + " no existe"));
        }
        return ResponseEntity.ok( GenericResponse.success( "Rol obtenido correctamente", RoleMapper.toDto(role.get())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<RoleDto>> update(@PathVariable Long id, @RequestBody RoleDto updatedRole) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body( GenericResponse.failure( "El rol con ID " + id + " no existe"));
        }

        Role role = roleOpt.get();

        // Validar nombre duplicado en otro registro
        if (roleRepository.existsByName(updatedRole.getName())
                && !role.getName().equalsIgnoreCase(updatedRole.getName())) {
            return ResponseEntity
                    .badRequest()
                    .body( GenericResponse.failure( "El rol '" + updatedRole.getName() + "' ya existe"));
        }

        role.setName(updatedRole.getName());
        Role saved = roleRepository.save(role);

        return ResponseEntity.ok( GenericResponse.success( "Rol actualizado correctamente", RoleMapper.toDto(saved)));
    }
}
