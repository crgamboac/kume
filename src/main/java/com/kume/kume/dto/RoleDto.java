package com.kume.kume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RoleDto {
    private Long id;
    @NotBlank(message = "Role name is mandatory")
    private String name;
}
