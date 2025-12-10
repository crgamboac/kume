package com.kume.kume.presentation.mappers;

import org.springframework.stereotype.Component;

import com.kume.kume.application.dto.user.CreateUserRequest;
import com.kume.kume.infraestructure.models.User;
@Component
public class UserMapper {
    public  CreateUserRequest toDto(User user) {
        if (user == null) {
            return new CreateUserRequest();
        }

        return CreateUserRequest.builder()
                .fullName(user.getFullName())
                .username(user.getUsername())
                .build();
    }
    public  User toEntity(CreateUserRequest userDto) {
        if (userDto == null) {
            return new User();
        }

        User user = new User();
        user.setFullName(userDto.getFullName());
        user.setUsername(userDto.getUsername());
        return user;
    }
}
