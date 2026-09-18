package demo;

import java.nio.file.Path;
import java.util.Arrays;

public final class Main {

    public static void main(final String[] args) {
        final Path image = readImagePathFromArgs(args);
        System.out.println("image> " + image);

        final ModelGateway gateway = ModelGateway.create();
        final String description = gateway.describeImage(image);
        System.out.println("description> " + description);
    }

    private static Path readImagePathFromArgs(final String[] args) {
        return Arrays.stream(args)
                .map(Path::of)
                .findFirst()
                .orElse(Path.of("assets", "images", "Image-1.jpg"));
    }
}
