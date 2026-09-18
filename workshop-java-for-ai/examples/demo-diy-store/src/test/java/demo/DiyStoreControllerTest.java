package demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiyStoreController.class)
class DiyStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void displaysTheRequestForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("productSearch"))
                .andExpect(content().string(containsString("DIY Store")));
    }

    @Test
    void redisplaysTheFormWhenTheSearchIsBlank() throws Exception {
        mockMvc.perform(get("/search").param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("productSearch", "query"))
                .andExpect(content().string(containsString("Enter a product name, keyword, or DIY project.")))
                .andExpect(content().string(containsString("aria-invalid=\"true\"")));
    }

    @Test
    void findsProductsByNameOrKeyword() throws Exception {
        org.mockito.BDDMockito.given(productService.searchProducts("living room"))
                .willReturn(List.of(new Product(1, "Interior matt paint", "paint, wall, living room", "Durable white paint for interior walls.")));

        mockMvc.perform(get("/search").param("query", "living room"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Interior matt paint")))
                .andExpect(content().string(containsString("Search results")));
    }

    @Test
    void findsProductsByName() throws Exception {
        org.mockito.BDDMockito.given(productService.searchProducts("screws"))
                .willReturn(List.of(new Product(2, "Wood screws", "screw, wood, fixings", "Multipurpose screws for indoor wood projects.")));

        mockMvc.perform(get("/search").param("query", "screws"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Wood screws")));
    }
}
