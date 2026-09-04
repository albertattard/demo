package demo;

record Prompt(String prompt) {

    static Prompt of(final String prompt) {
        return new Prompt(prompt);
    }
}
