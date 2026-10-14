package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ModelPromptTemplates {

    private final Resource recommendationSystemPrompt;
    private final Resource taskInferenceSystemPrompt;
    private final Resource taskInferenceUserPrompt;

    public ModelPromptTemplates(
            @Value("classpath:prompts/recommendation-system.st") final Resource recommendationSystemPrompt,
            @Value("classpath:prompts/task-inference-system.st") final Resource taskInferenceSystemPrompt,
            @Value("classpath:prompts/task-inference-user.st") final Resource taskInferenceUserPrompt) {
        this.recommendationSystemPrompt = recommendationSystemPrompt;
        this.taskInferenceSystemPrompt = taskInferenceSystemPrompt;
        this.taskInferenceUserPrompt = taskInferenceUserPrompt;
    }

    public Resource recommendationSystemPrompt() {
        return recommendationSystemPrompt;
    }

    public Resource taskInferenceSystemPrompt() {
        return taskInferenceSystemPrompt;
    }

    public Resource taskInferenceUserPrompt() {
        return taskInferenceUserPrompt;
    }
}
