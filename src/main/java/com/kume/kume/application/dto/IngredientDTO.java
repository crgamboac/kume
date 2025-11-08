package com.kume.kume.application.dto;
import com.kume.kume.infraestructure.models.Ingredient;
import com.kume.kume.infraestructure.models.IngredientType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {
    private Long id;
    private String name;
    private IngredientType ingredientType;

    public static IngredientDTO fromEntity(Ingredient ingredient) {
        IngredientDTO dto = new IngredientDTO();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setIngredientType(ingredient.getIngredientType());
        return dto;
    }

    public Ingredient toEntity() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(this.id);
        ingredient.setName(this.name);
        ingredient.setIngredientType(this.ingredientType);
        return ingredient;
    }
}
