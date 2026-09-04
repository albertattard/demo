package demo;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ModelGateway modelGateway = mock(ModelGateway.class);
    private final ImageResizer imageResizer = mock(ImageResizer.class);
    private final ProductService service = new ProductService(repository, modelGateway, imageResizer);

    @Test
    void findsAndDeduplicatesProductsForTheRecommendedCategories() {
        final Product paint = new Product(1, "Interior matt paint", "paint, wall", "Durable paint for interior walls.");
        final Product brush = new Product(2, "Paint brush", "paint, brush", "A brush for interior paint.");
        when(modelGateway.recommend("I would like to paint my living room."))
                .thenReturn(new RecommendationBrief(true, List.of("paint", "brush")));
        when(repository.search("paint")).thenReturn(List.of(paint, brush));
        when(repository.search("brush")).thenReturn(List.of(brush));

        final List<Product> products = service.recommendProducts("I would like to paint my living room.");

        assertEquals(List.of(paint, brush), products);
        verify(repository).search("paint");
        verify(repository).search("brush");
    }

    @Test
    void rendersTaskChoicesWhenTheJudgePassesThem() throws Exception {
        final byte[] original = {1, 2, 3};
        final byte[] resized = {4, 5, 6};
        final DiyTaskInference expected = new DiyTaskInference(true, List.of("Paint an interior wall"));
        when(imageResizer.resizeToLongestSide(original, 1_024)).thenReturn(resized);
        when(modelGateway.inferTasks(resized, MediaType.IMAGE_JPEG)).thenReturn(expected);
        when(modelGateway.judgeTaskChoices(expected))
                .thenReturn(new TaskInferenceSafetyReview(TaskInferenceSafetyReview.JudgeDecision.PASSED,
                        "The task choices are supported and appropriate."));

        final DiyTaskInference actual = service.inferTasks(original, MediaType.IMAGE_JPEG);

        assertSame(expected, actual);
        verify(imageResizer).resizeToLongestSide(same(original), eq(1_024));
        verify(modelGateway).inferTasks(same(resized), same(MediaType.IMAGE_JPEG));
        verify(modelGateway).judgeTaskChoices(same(expected));
    }

    @Test
    void hidesTaskChoicesWhenTheJudgeRejectsThem() throws Exception {
        final DiyTaskInference expected = new DiyTaskInference(true, List.of("Rejected task text"));
        when(imageResizer.resizeToLongestSide(org.mockito.ArgumentMatchers.any(), eq(1_024)))
                .thenReturn(new byte[]{4, 5, 6});
        when(modelGateway.inferTasks(any(), eq(MediaType.IMAGE_JPEG))).thenReturn(expected);
        when(modelGateway.judgeTaskChoices(expected))
                .thenReturn(new TaskInferenceSafetyReview(TaskInferenceSafetyReview.JudgeDecision.REJECTED,
                        "The task choices are inappropriate."));

        final DiyTaskInference actual = service.inferTasks(new byte[]{1, 2, 3}, MediaType.IMAGE_JPEG);

        assertFalse(actual.supported());
        assertTrue(actual.taskChoices().isEmpty());
    }
}
