package demo;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

final class ModelService {

    private final ModelGateway gateway;

    static ModelService create() {
        return create(ModelGateway.create());
    }

    static ModelService create(final ModelGateway gateway) {
        requireNonNull(gateway);
        return new ModelService(gateway);
    }

    Result prompt(final String prompt) {
        requireNonNull(prompt);

        final String assistant = gateway.chat(prompt);

        final String system = """
                You are acting as an impartial evaluator.
                Please assess the following text based on the following four criteria:
                – Factual Accuracy: Are the claims supported by evidence or generally accepted facts?
                – Clarity: Is the text easy to understand and well-structured?
                – Logical Coherence: Does the reasoning follow a consistent and logical structure?
                – Persuasiveness: Is the argument compelling and well-supported?
                
                Provide an overall score from 1 to 10, where:
                – 1 indicates a very poor text across all criteria
                – 10 indicates an excellent text that performs strongly in all areas
                
                Only return the score.""";

        /* TODO: Should we use templates? */
        final String judgePrompt = """                
                Text to Evaluate:
                Prompt: ${prompt}
                Assistant: ${assistant}"""
                .replace("${prompt}", prompt.translateEscapes())
                .replace("${assistant}", assistant.translateEscapes());

        final String judge = gateway.judge(system, judgePrompt);

        final Pattern regex = Pattern.compile("^\\d{1,2}$");
        if (regex.matcher(judge).matches()) {
            final int score = Integer.parseInt(judge);
            if (score >= 1 && score <= 10) {
                return Result.ok(prompt, assistant, score);
            }
        }

        return Result.error(prompt, assistant, judge);
    }

    private ModelService(final ModelGateway gateway) {
        this.gateway = gateway;
    }

    sealed interface Result {

        record Ok(String prompt, String assistant, int score) implements Result {}

        record Error(String prompt, String assistant, String judge) implements Result {}

        static Result ok(final String prompt, final String assistant, final int score) {
            return new Ok(prompt, assistant, score);
        }

        static Result error(final String prompt, final String assistant, final String judge) {
            return new Error(prompt, assistant, judge);
        }
    }
}
