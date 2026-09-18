package demo;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

@Service
class ModelGateway {

    private final TextToSpeechModel speech;
    private final TranscriptionModel transcription;

    ModelGateway(final TextToSpeechModel speech, final TranscriptionModel transcription) {
        this.speech = speech;
        this.transcription = transcription;
    }

    byte[] synthesis(final String text) {
        requireNonNull(text);
        return speech.call(text);
    }

    String transcribe(final byte[] audio) {
        requireNonNull(audio);

        final String languageHint = "en"; /* https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes */
        final OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .language(languageHint)
                .temperature(0f)
                .build();

        /* The file name cannot be null, thus we have to override this! */
        final Resource audioResource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return "audio.mp3";
            }
        };

        return transcription.call(new AudioTranscriptionPrompt(audioResource, options))
                .getResult()
                .getOutput();
    }
}
