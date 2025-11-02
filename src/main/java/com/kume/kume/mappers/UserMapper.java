package com.kume.kume.mappers;

import com.kume.kume.dto.UserDto;
import com.kume.kume.models.User;

public class UserMapper {
    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .full_name(user.getFull_name())
                .email(user.getEmail())
                .dni(user.getDni())
                .role(RoleMapper.toDto(user.getRole()))
                .build();
    }
    public static User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        User user = new User();
        user.setId(userDto.getId());
        user.setFull_name(userDto.getFull_name());
        user.setEmail(userDto.getEmail());
        user.setDni(userDto.getDni());
       user.setRole(RoleMapper.toEntity(userDto.getRole()));
        return user;
    }
}
