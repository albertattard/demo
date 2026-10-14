package demo.chat;

public record AssistantReference(String title,
                                 String subtitle,
                                 String slug,
                                 String path,
                                 double score) implements Comparable<AssistantReference> {
    @Override
    public int compareTo(final AssistantReference other) {
        return Double.compare(other.score, score);
    }

    public AssistantReference max(final AssistantReference other) {
        return score >= other.score
                ? this
                : other;
    }
}
