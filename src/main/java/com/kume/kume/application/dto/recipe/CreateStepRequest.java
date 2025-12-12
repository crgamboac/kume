package com.kume.kume.application.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStepRequest {
    @NotNull(message = "El número de paso es obligatorio")
    private Long stepNumber;
    @NotBlank(message = "La instrucción es obligatoria")
    private String instruction;
}
