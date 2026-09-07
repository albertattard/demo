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
    void returnTheItemByGuidId() throws Exception {
        mockMvc.perform(get("/catalogue/item/81994f73-50c3-4035-b4e3-e81c5c250ddd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.guid").value("81994f73-50c3-4035-b4e3-e81c5c250ddd"))
                .andExpect(jsonPath("$.caption").value("Leather Sofa"))
                .andExpect(jsonPath("$.description").value("A very nice and comfortable sofa"));
    }

    @Test
    void returnNotFoundWhenItemWithGivenGuidDoesNotExist() throws Exception {
        mockMvc.perform(get("/catalogue/item/17c15427-96a6-4457-8541-5346d53d4d9d"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnBadRequestWhenGivenNonUuid() throws Exception {
        mockMvc.perform(get("/catalogue/item/1"))
                .andExpect(status().isBadRequest());
    }
}
