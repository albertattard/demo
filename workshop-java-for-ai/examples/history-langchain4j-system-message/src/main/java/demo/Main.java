package demo;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

public final class Main {

    public static void main(final String[] args) {
        final MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();

        chatMemory.add(SystemMessage.systemMessage("The system message..."));

        for (int i = 1; i <= 15; i++) {
            chatMemory.add(UserMessage.from("User message " + i));
        }

        System.out.println("The history has " + chatMemory.messages().size() + " messages");
        chatMemory.messages()
                .forEach(m -> System.out.println(" - " + m + " (of type " + m.getClass().getSimpleName() + ")"));
    }

    private Main() {}
}
