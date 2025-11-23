package com.kume.kume.application.dto;

import com.kume.kume.application.dto.recipe.RecipeResponse;

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
    public static Result<RecipeResponse> error(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'error'");
    }
}
