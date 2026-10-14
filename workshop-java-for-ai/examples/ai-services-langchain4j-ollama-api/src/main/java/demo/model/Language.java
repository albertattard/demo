package demo.model;

public enum Language {
    GERMAN("German"),
    ;

    private final String toString;

    Language(final String toString) {
        this.toString = toString;
    }

    @Override
    public String toString() {
        return toString;
    }
}
