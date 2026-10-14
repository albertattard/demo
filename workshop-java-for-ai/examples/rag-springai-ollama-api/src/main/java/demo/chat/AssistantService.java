package demo.chat;

import java.util.List;

@FunctionalInterface
public interface AssistantService {

    AssistantResponse reply(List<DialogueMessage> history, String userText);
}
