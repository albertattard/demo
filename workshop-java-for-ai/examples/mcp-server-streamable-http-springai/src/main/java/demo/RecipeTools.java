package demo;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class RecipeTools {

    private final RecipeRepository repository;

    private RecipeTools(final RecipeRepository repository) {
        this.repository = repository;
    }

    @Tool(name = "recipeCount", description = "Returns the count of recipes in the repository.")
    public long recipeCount() {
        return repository.count();
    }

    @Tool(name = "findRecipesByType", description = "Finds recipes of type, such as DESSERT, MAIN, or STARTER")
    public List<Recipe> findRecipesByType(@ToolParam(description = "The type of recipe") final RecipeType type) {
        return repository.findAllByType(type);
    }
}
