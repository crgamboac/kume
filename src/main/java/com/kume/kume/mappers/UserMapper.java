package com.kume.kume.mappers;

import com.kume.kume.dto.user.CreateUserRequest;
import com.kume.kume.models.User;

public class UserMapper {
    public static CreateUserRequest toDto(User user) {
        if (user == null) {
            return null;
        }

        return CreateUserRequest.builder()
                .fullName(user.getFullName())
                .username(user.getUsername())
                .build();
    }
    public static User toEntity(CreateUserRequest userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setFullName(userDto.getFullName());
        user.setUsername(userDto.getUsername());
        return user;
    }
}
