package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ModelPromptTemplates {

    private final Resource recommendationSystemPrompt;
    private final Resource taskInferenceSystemPrompt;
    private final Resource taskInferenceUserPrompt;
    private final Resource taskJudgeSystemPrompt;
    private final Resource taskJudgeUserPrompt;

    public ModelPromptTemplates(
            @Value("classpath:prompts/recommendation-system.st") final Resource recommendationSystemPrompt,
            @Value("classpath:prompts/task-inference-system.st") final Resource taskInferenceSystemPrompt,
            @Value("classpath:prompts/task-inference-user.st") final Resource taskInferenceUserPrompt,
            @Value("classpath:prompts/task-judge-system.st") final Resource taskJudgeSystemPrompt,
            @Value("classpath:prompts/task-judge-user.st") final Resource taskJudgeUserPrompt) {
        this.recommendationSystemPrompt = recommendationSystemPrompt;
        this.taskInferenceSystemPrompt = taskInferenceSystemPrompt;
        this.taskInferenceUserPrompt = taskInferenceUserPrompt;
        this.taskJudgeSystemPrompt = taskJudgeSystemPrompt;
        this.taskJudgeUserPrompt = taskJudgeUserPrompt;
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

    public Resource taskJudgeSystemPrompt() {
        return taskJudgeSystemPrompt;
    }

    public Resource taskJudgeUserPrompt() {
        return taskJudgeUserPrompt;
    }
}
