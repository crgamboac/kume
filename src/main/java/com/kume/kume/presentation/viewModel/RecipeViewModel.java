package com.kume.kume.presentation.viewModel;

public record RecipeViewModel(
        Long id,
        String name,
        Long cookingTime,
        String difficulty,
        String imageUrl

) {

}
