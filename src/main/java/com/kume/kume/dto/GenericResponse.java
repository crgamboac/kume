package com.kume.kume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GenericResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
}
