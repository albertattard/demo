package demo;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ImageResizer {

    public byte[] resizeToLongestSide(final byte[] image, final int maximumDimension) throws IOException {
        if (maximumDimension < 1) {
            throw new IllegalArgumentException("Maximum image dimension must be positive.");
        }

        final BufferedImage source = ImageIO.read(new ByteArrayInputStream(image));
        if (source == null) {
            throw new IOException("The uploaded JPEG image could not be read.");
        }

        final int longestSide = Math.max(source.getWidth(), source.getHeight());
        if (longestSide <= maximumDimension) {
            return image;
        }

        final double scale = (double) maximumDimension / longestSide;
        final int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        final int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        final BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(resized, "jpeg", output)) {
            throw new IOException("No JPEG writer is available.");
        }
        return output.toByteArray();
    }
}
