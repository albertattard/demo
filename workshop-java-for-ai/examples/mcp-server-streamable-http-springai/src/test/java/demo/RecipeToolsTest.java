package demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTestWithTestProfile
class RecipeToolsTest {

    @Autowired
    private RecipeTools tools;

    @Test
    void returnTwoRecipes() {
        /* Given */
        final RecipeType type = RecipeType.MAIN;

        /* When */
        final List<String> result = tools.findRecipesByType(type).stream()
                .map(Recipe::title)
                .toList();

        /* Then */
        assertThat(result)
                .isEqualTo(List.of("Classic Garlic & Rosemary Roast Lamb", "Creamy Chickpea & Spinach Coconut Curry"));
    }
}
