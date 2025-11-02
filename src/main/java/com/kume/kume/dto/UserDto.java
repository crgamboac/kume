package com.kume.kume.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserDto {
    
    private Long id;
    @NotBlank(message = "Full name is mandatory")
    private String full_name;
    @NotBlank(message = "Email is mandatory")
    private String email;
    @NotBlank(message = "DNI is mandatory")
    private String dni;
    @NotBlank(message = "Role name is mandatory")
        @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

    private RoleDto role;
}
