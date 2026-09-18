package demo.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public record ParsedResponse(String text, List<String> references) {

    private static final String START_MARKER = "---SOURCES_JSON---";
    private static final String END_MARKER = "---END_SOURCES_JSON---";

    // Fuzzy single-line markers (handle missing/extra dashes & whitespace)
    private static final Pattern START_BLOCK =
            Pattern.compile("(?m)^\\s*-*\\s*SOURCES_JSON\\s*-*\\s*$");
    private static final Pattern END_BLOCK =
            Pattern.compile("(?m)^\\s*-*\\s*END_SOURCES_JSON\\s*-*\\s*$");

    // Standalone markdown HR line
    private static final Pattern HR_LINE = Pattern.compile("(?m)^\\s*---\\s*$\\R?");
    // Footnote markers like [^/guide/password-reset-guide]
    private static final Pattern FOOTNOTE = Pattern.compile("\\[\\^[^]]+]");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ParsedResponse parse(final String response) {
        requireNonNull(response, "Cannot parse a null response");

        // 1) Try strict markers first
        final int strictStart = response.indexOf(START_MARKER);
        final int strictEnd = response.indexOf(END_MARKER);

        if (strictStart != -1 && strictEnd != -1 && strictStart < strictEnd) {
            final String body = response.substring(0, strictStart);
            final String json = response.substring(strictStart + START_MARKER.length(), strictEnd).trim();
            return new ParsedResponse(cleanBody(body), parseReferences(json));
        }

        // 2) Try fuzzy line-based markers
        final Matcher startM = START_BLOCK.matcher(response);
        final Matcher endM = END_BLOCK.matcher(response);

        Integer fuzzyStartLineStart = null;   // where the start line begins
        Integer fuzzyStartJsonFrom = null;    // first char after the start line (JSON should begin here)
        if (startM.find()) {
            fuzzyStartLineStart = startM.start();
            fuzzyStartJsonFrom = startM.end();
        }

        Integer fuzzyEndLineStart = null;     // where the END line begins
        if (endM.find()) {
            fuzzyEndLineStart = endM.start();
        }

        if (fuzzyStartJsonFrom != null && fuzzyEndLineStart != null && fuzzyStartJsonFrom <= fuzzyEndLineStart) {
            final String body = response.substring(0, fuzzyStartLineStart);
            final String json = response.substring(fuzzyStartJsonFrom, fuzzyEndLineStart).trim();
            return new ParsedResponse(cleanBody(body), parseReferences(json));
        }

        // 3) Fallback: if we at least have an END line, try to grab the last JSON object before it,
        //    even if the START line is malformed/missing (e.g., "SOURCES_JSON---").
        if (fuzzyEndLineStart != null) {
            // Look for the last {...} that ends before the END line
            final int searchEnd = fuzzyEndLineStart;
            final int lastClose = response.lastIndexOf('}', searchEnd);
            final int lastOpen = (lastClose == -1)
                    ? -1
                    : response.lastIndexOf('{', lastClose);
            if (lastOpen != -1 && lastClose >= lastOpen) {
                final String json = response.substring(lastOpen, lastClose + 1).trim();

                // Set body to everything before the line that contains this JSON,
                // also trimming any dangling "SOURCES_JSON---" or similar label line above the JSON.
                int bodyEnd = lineStartBefore(response, lastOpen);
                final String body = response.substring(0, bodyEnd);
                final List<String> refs = parseReferences(json);
                if (!refs.isEmpty()) {
                    return new ParsedResponse(cleanBody(body), refs);
                }
            }
        }

        // 4) Nothing workable found: clean body & return no refs
        return new ParsedResponse(cleanBody(response), List.of());
    }

    private static int lineStartBefore(final String s, final int pos) {
        final int nl = s.lastIndexOf('\n', Math.max(0, pos - 1));
        return (nl == -1) ? 0 : nl + 1;
    }

    private static String cleanBody(final String body) {
        String cleaned = body;

        // Remove standalone '---' lines (markdown horizontal rule)
        cleaned = HR_LINE.matcher(cleaned).replaceAll("");

        // Remove footnote markers like [^/guide/password-reset-guide]
        cleaned = FOOTNOTE.matcher(cleaned).replaceAll("");

        // Trim trailing spaces on lines, then overall trim
        cleaned = cleaned.replaceAll("[ \\t]+(?=\\R)", "");
        cleaned = cleaned.trim();

        return cleaned;
    }

    private static List<String> parseReferences(final String json) {
        if (json.isBlank() || "{}".equals(json.trim())) {
            return List.of();
        }

        try {
            final JsonNode root = MAPPER.readTree(json);
            return collectIds(root).stream()
                    .toList();
        } catch (final JsonProcessingException e) {
            // If JSON is malformed, be forgiving and just return no references.
            return List.of();
        }
    }

    /**
     * Collect all text values under fields named "id" at any depth.
     */
    private static Set<String> collectIds(final JsonNode node) {
        final Set<String> ids = new LinkedHashSet<>();

        final Deque<JsonNode> stack = new ArrayDeque<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            final JsonNode current = stack.pop();
            if (current.isObject()) {
                current.properties().forEach(entry -> {
                    final String name = entry.getKey();
                    final JsonNode value = entry.getValue();
                    if ("id".equals(name) && value.isTextual()) {
                        final String id = value.asText().trim();
                        if (!id.isEmpty()) {ids.add(id);}
                    }
                    stack.push(value);
                });
            } else if (current.isArray()) {
                current.forEach(stack::push);
            }
        }

        return ids;
    }
}
