package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final ChatClient client;

    ModelGateway(final ChatClient.Builder builder) {
        this.client = builder.build();
    }

    String describeImage(final Path path) {
        requireExistingFile(path);

        final Media media = Media.builder()
                .data(new FileSystemResource(path))
                .mimeType(MediaTypeFactory.getMediaType(new FileSystemResource(path))
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .build();

        return client.prompt()
                .user(spec -> spec
                        .text("Describe what you see in the attached image in a sentence or two.")
                        .media(media))
                .call()
                .content()
                .trim();
    }

    private static void requireExistingFile(final Path path) {
        requireNonNull(path);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("The path '" + path + "' is not a regular file");
        }
    }
}
