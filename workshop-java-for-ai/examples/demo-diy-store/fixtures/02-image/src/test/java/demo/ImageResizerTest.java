package demo;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageResizerTest {

    private final ImageResizer imageResizer = new ImageResizer();

    @Test
    void resizesTheLongestSideToTheMaximumDimension() throws Exception {
        final byte[] resized = imageResizer.resizeToLongestSide(jpeg(2_000, 1_000), 1_024);
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(resized));

        assertEquals(1_024, image.getWidth());
        assertEquals(512, image.getHeight());
    }

    @Test
    void preservesASmallImageWithoutReencodingIt() throws Exception {
        final byte[] original = jpeg(800, 600);

        assertArrayEquals(original, imageResizer.resizeToLongestSide(original, 1_024));
    }

    private byte[] jpeg(final int width, final int height) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "jpeg", output));
        return output.toByteArray();
    }
}
