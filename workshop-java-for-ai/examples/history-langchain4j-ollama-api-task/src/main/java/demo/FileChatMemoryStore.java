package demo;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static java.util.Objects.requireNonNull;

public final class FileChatMemoryStore implements ChatMemoryStore {

    private final Path directory;

    public static ChatMemoryStore create(final String directory) {
        requireNonNull(directory);
        return create(Path.of(directory));
    }

    public static ChatMemoryStore create(final Path directory) {
        requireNonNull(directory);
        return new FileChatMemoryStore(directory);
    }

    private FileChatMemoryStore(final Path directory) {
        this.directory = directory;
    }

    @Override
    public synchronized List<ChatMessage> getMessages(final Object memoryId) {
        final Path file = file(memoryId);
        if (!Files.exists(file)) return new ArrayList<>();

        final String json = readStringFromFile(file);
        return messagesFromJson(json);
    }

    @Override
    public synchronized void updateMessages(final Object memoryId, final List<ChatMessage> messages) {
        final Path file = file(memoryId);
        createMissingParentDirectories(file);

        final String json = messagesToJson(messages);
        writeStringToFile(json, file);
    }

    @Override
    public synchronized void deleteMessages(final Object memoryId) {
        final Path file = file(memoryId);
        deleteFile(file);
    }

    private Path file(final Object memoryId) {
        return directory.resolve("chat-" + memoryId + ".json");
    }

    private static void createMissingParentDirectories(final Path file) {
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file.getParent());
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to create missing parent directories" + file, e);
            }
        }
    }

    private static String readStringFromFile(final Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read file " + file, e);
        }
    }

    private static void writeStringToFile(final String text, final Path file) {
        try {
            Files.writeString(file, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to write to file " + file, e);
        }
    }

    private static void deleteFile(final Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to delete file " + file, e);
        }
    }
}
