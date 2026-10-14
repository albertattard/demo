package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ModelPromptTemplates {

    private final Resource taskInferenceSystemPrompt;
    private final Resource taskInferenceUserPrompt;
    private final Resource taskJudgeSystemPrompt;
    private final Resource taskJudgeUserPrompt;
    private final Resource retrievalQueriesSystemPrompt;
    private final Resource productSelectionSystemPrompt;
    private final Resource productSelectionUserPrompt;

    public ModelPromptTemplates(
            @Value("classpath:prompts/task-inference-system.st") final Resource taskInferenceSystemPrompt,
            @Value("classpath:prompts/task-inference-user.st") final Resource taskInferenceUserPrompt,
            @Value("classpath:prompts/task-judge-system.st") final Resource taskJudgeSystemPrompt,
            @Value("classpath:prompts/task-judge-user.st") final Resource taskJudgeUserPrompt,
            @Value("classpath:prompts/retrieval-queries-system.st") final Resource retrievalQueriesSystemPrompt,
            @Value("classpath:prompts/product-selection-system.st") final Resource productSelectionSystemPrompt,
            @Value("classpath:prompts/product-selection-user.st") final Resource productSelectionUserPrompt) {
        this.taskInferenceSystemPrompt = taskInferenceSystemPrompt;
        this.taskInferenceUserPrompt = taskInferenceUserPrompt;
        this.taskJudgeSystemPrompt = taskJudgeSystemPrompt;
        this.taskJudgeUserPrompt = taskJudgeUserPrompt;
        this.retrievalQueriesSystemPrompt = retrievalQueriesSystemPrompt;
        this.productSelectionSystemPrompt = productSelectionSystemPrompt;
        this.productSelectionUserPrompt = productSelectionUserPrompt;
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

    public Resource retrievalQueriesSystemPrompt() {
        return retrievalQueriesSystemPrompt;
    }

    public Resource productSelectionSystemPrompt() {
        return productSelectionSystemPrompt;
    }

    public Resource productSelectionUserPrompt() {
        return productSelectionUserPrompt;
    }
}
