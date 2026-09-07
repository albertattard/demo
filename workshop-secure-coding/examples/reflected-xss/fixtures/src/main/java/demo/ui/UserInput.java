package demo.ui;

import org.owasp.encoder.Encode;

public record UserInput(String message) {
    public String getHtmlEncodedMessage() {
        return Encode.forHtml(message);
    }
}
