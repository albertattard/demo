package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters.AudioResponseFormat;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters.Voice;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
class ModelGateway {

    private final ChatClient client;

    ModelGateway(final ChatClient.Builder builder) {
        this.client = builder.build();
    }

    PromptResult prompt(final Path prompt) {
        final ChatResponse response = client.prompt()
                .user(spec -> spec
                        .text("Answer the question from the attached audio file.")
                        .media(Media.builder()
                                .data(new FileSystemResource(prompt))
                                .mimeType(MimeTypeUtils.parseMimeType("audio/mp3"))
                                .build()))
                .options(OpenAiChatOptions.builder()
                        .outputModalities(List.of("text", "audio"))
                        .outputAudio(new AudioParameters(Voice.ALLOY, AudioResponseFormat.MP3))
                        .build())
                .call()
                .chatResponse();

        // Assistant message with transcript + audio media
        final AssistantMessage assistantMessage = response.getResult()
                .getOutput();

        final String transcript = assistantMessage.getText();
        final byte[] audio = assistantMessage
                .getMedia()
                .getFirst()
                .getDataAsByteArray();
        return new PromptResult(transcript, audio);
    }

    record PromptResult(String transcript, byte[] audio) {
        @Override
        public boolean equals(final Object object) {
            return object instanceof PromptResult(String otherTranscript, byte[] otherAudio)
                   && Arrays.equals(audio, otherAudio)
                   && Objects.equals(transcript, otherTranscript);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transcript, Arrays.hashCode(audio));
        }

        @Override
        public String toString() {
            return "PromptResult[transcript=" + transcript + ']';
        }
    }
}
