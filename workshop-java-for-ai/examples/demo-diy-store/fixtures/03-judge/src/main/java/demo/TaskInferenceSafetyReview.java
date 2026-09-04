package demo;

public record TaskInferenceSafetyReview(
        JudgeDecision decision,
        String reason) {

    public boolean passed() {
        return decision == JudgeDecision.PASSED;
    }

    public enum JudgeDecision {
        PASSED,
        REJECTED
    }
}
