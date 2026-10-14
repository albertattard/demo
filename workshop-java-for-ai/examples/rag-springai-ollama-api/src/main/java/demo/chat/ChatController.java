package demo.chat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@RequestMapping("/chat")
@SessionAttributes({"history"})
public class ChatController {

    private final AssistantService service;

    public ChatController(final AssistantService service) {
        this.service = service;
    }

    @ModelAttribute("history")
    List<DialogueMessage> history() {
        return new CopyOnWriteArrayList<>();
    }

    @GetMapping
    public String page(final @ModelAttribute("history") List<DialogueMessage> history,
                       final Model model) {
        model.addAttribute("history", history);
        return "chat";
    }

    @PostMapping("/message")
    public String postMessage(final @RequestParam("text") String text,
                              final @ModelAttribute("history") List<DialogueMessage> history,
                              final Model model) {

        final AssistantResponse reply = service.reply(history, text);

        final DialogueMessage user = DialogueMessage.user(text);
        history.add(user);

        final String html = MarkdownToHtmlUtils.toHtml(reply.text());
        final DialogueMessage assistant = DialogueMessage.assistant(html, reply.references());
        history.add(assistant);

        model.addAttribute("user", user);
        model.addAttribute("assistant", assistant);
        return "fragments/chat-message :: pair";
    }

    @PostMapping("/reset")
    public String reset(final @ModelAttribute("history") List<DialogueMessage> history) {
        history.clear();
        return "fragments/chat-reset.html :: cleared";
    }
}
