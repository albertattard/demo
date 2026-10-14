package demo.chat;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

public final class MarkdownToHtmlUtils {

    private static final Parser PARSER = Parser.builder().build();

    private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
            .escapeHtml(false)
            .build();

    public static String toHtml(final String markdown) {
        final Node doc = PARSER.parse(markdown);
        return RENDERER.render(doc).trim();
    }
}
