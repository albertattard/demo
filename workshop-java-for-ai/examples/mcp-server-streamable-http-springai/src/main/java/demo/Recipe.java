package demo;

import org.springframework.data.annotation.Id;

public record Recipe(
        @Id
        Long id,
        String title,
        String description,
        RecipeType type,
        boolean vegan,
        Integer timeNeededMinutes) {}
