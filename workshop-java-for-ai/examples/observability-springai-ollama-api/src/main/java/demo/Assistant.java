package demo;

record Assistant(String assistant) {

    static Assistant of(String assistant) {
        return new Assistant(assistant);
    }
}
