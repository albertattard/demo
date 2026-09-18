package demo;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecipeRepository extends CrudRepository<Recipe, Long> {

    List<Recipe> findAllByType(RecipeType type);

    @Query("SELECT title FROM recipe ORDER BY title")
    List<String> findAllTitles();
}
