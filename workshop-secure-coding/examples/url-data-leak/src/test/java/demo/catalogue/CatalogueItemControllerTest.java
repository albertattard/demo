package demo.catalogue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogueItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnTheItemById() throws Exception {
        mockMvc.perform(get("/catalogue/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.caption").value("Leather Sofa"))
                .andExpect(jsonPath("$.description").value("A very nice and comfortable sofa"));
    }

    @Test
    void returnNotFoundWhenItemWithGivenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/catalogue/item/9999"))
                .andExpect(status().isNotFound());
    }
}
