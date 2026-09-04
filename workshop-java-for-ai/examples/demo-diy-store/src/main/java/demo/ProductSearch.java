package demo;

import jakarta.validation.constraints.NotBlank;

public class ProductSearch {

    @NotBlank(message = "Enter a product name, keyword, or DIY project.")
    private String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }
}
