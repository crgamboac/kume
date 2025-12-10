package com.kume.kume.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Result<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, message, data);
    }
    public static <T> Result<T> failure(String message) {
        return new Result<>(false, message, null);
    }
  
}
