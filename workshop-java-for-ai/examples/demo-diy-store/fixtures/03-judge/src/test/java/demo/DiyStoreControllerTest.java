package demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void redisplaysTheSharedFormWhenTheRecommendationRequestIsBlank() throws Exception {
        mockMvc.perform(post("/recommend").param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("productSearch", "query"))
                .andExpect(content().string(containsString("Enter a product name, keyword, or DIY project.")));
    }

    @Test
    void displaysCatalogueProductsForTheRecommendation() throws Exception {
        org.mockito.BDDMockito.given(productService.recommendProducts("I would like to paint my living room."))
                .willReturn(List.of(new Product(1, "Interior matt paint", "paint, wall, living room", "Durable white paint for interior walls.")));

        mockMvc.perform(post("/recommend").param("query", "I would like to paint my living room."))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("products"))
                .andExpect(content().string(containsString("Interior matt paint")))
                .andExpect(content().string(containsString("Search results")));
    }

    @Test
    void rejectsAnEmptyPhotoWithoutCallingTheModel() throws Exception {
        mockMvc.perform(multipart("/infer-tasks")
                        .file(new MockMultipartFile("photo", "", "image/jpeg", new byte[0])))
                .andExpect(status().isOk())
                .andExpect(model().attribute("photoError", "Choose a JPEG image to analyse."));

        verifyNoInteractions(productService);
    }

    @Test
    void rejectsANonJpegImageWithoutCallingTheModel() throws Exception {
        mockMvc.perform(multipart("/infer-tasks")
                        .file(new MockMultipartFile("photo", "room.png", "image/png", new byte[]{1})))
                .andExpect(status().isOk())
                .andExpect(model().attribute("photoError", "Upload a JPEG image."));

        verifyNoInteractions(productService);
    }

    @Test
    void rejectsAnOversizedPhotoWithoutCallingTheModel() throws Exception {
        mockMvc.perform(multipart("/infer-tasks")
                        .file(new MockMultipartFile("photo", "room.jpg", "image/jpeg", new byte[5 * 1024 * 1024 + 1])))
                .andExpect(status().isOk())
                .andExpect(model().attribute("photoError", "Upload a JPEG image smaller than 5 MB."));

        verifyNoInteractions(productService);
    }

    @Test
    void rendersMockedTaskChoicesForASupportedPhoto() throws Exception {
        org.mockito.BDDMockito.given(productService.inferTasks(any(), eq(MediaType.IMAGE_JPEG)))
                .willReturn(new DiyTaskInference(true, List.of("Paint an interior wall", "Repair a loose skirting board")));

        mockMvc.perform(multipart("/infer-tasks")
                        .file(new MockMultipartFile("photo", "room.jpg", "image/jpeg", new byte[]{1, 2, 3})))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("taskInference"))
                .andExpect(content().string(containsString("Paint an interior wall")))
                .andExpect(content().string(containsString("Repair a loose skirting board")));
    }

    @Test
    void rendersTheFallbackWithoutRejectedTaskText() throws Exception {
        final String rejectedTask = "Rejected task text";
        org.mockito.BDDMockito.given(productService.inferTasks(any(), eq(MediaType.IMAGE_JPEG)))
                .willReturn(new DiyTaskInference(false, List.of()));

        mockMvc.perform(multipart("/infer-tasks")
                        .file(new MockMultipartFile("photo", "room.jpg", "image/jpeg", new byte[]{1, 2, 3})))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No supported DIY task could be identified from this photo.")))
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString(rejectedTask))));
    }
}
