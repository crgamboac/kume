package com.kume.kume.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kume.kume.dto.user.CreateUserRequest;
import com.kume.kume.mappers.UserMapper;
import com.kume.kume.models.Role;
import com.kume.kume.models.User;
import com.kume.kume.repositories.RoleRepository;
import com.kume.kume.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    private final RoleRepository roleRepository; 
    private final PasswordEncoder passwordEncoder;

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

        User user = UserMapper.toEntity(request);
        
        user.setPassword(hashedPassword);
        user.setRoles(roles);

        return userRepository.save(user);
    }
}