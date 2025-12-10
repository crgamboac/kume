package com.kume.kume.application.services;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kume.kume.application.dto.user.CreateUserRequest;
import com.kume.kume.infraestructure.models.Role;
import com.kume.kume.infraestructure.models.User;
import com.kume.kume.infraestructure.repositories.RoleRepository;
import com.kume.kume.infraestructure.repositories.UserRepository;
import com.kume.kume.presentation.mappers.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final RoleRepository roleRepository; 
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public User register(CreateUserRequest request) throws IllegalStateException {
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());

        if (existingUser.isPresent()) {
            throw new IllegalStateException("El nombre de usuario/email ya está registrado.");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> 
                new IllegalStateException("Rol 'ROLE_USER' no encontrado en la base de datos."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = userMapper.toEntity(request);
        
        user.setPassword(hashedPassword);
        user.setRoles(roles);

        return userRepository.save(user);
    }
}