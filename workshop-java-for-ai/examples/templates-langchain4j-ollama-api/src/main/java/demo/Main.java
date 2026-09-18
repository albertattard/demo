package demo;

import java.util.Arrays;

public final class Main {

    public static void main(final String[] args) {
        final String text = readTextToTranslateFromArgs(args);
        final Language language = readLanguageToTranslateTo(args);
        System.out.println("text> " + text);
        System.out.println("language> " + language);

        final ModelGateway gateway = ModelGateway.create();
        final String translated = gateway.translate(text, language);
        System.out.println("translated> " + translated.translateEscapes());
    }

    private static String readTextToTranslateFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("""
                        The first humans to land on the Moon were the \
                        astronauts of Apollo 11, Neil Armstrong and Buzz \
                        Aldrin, on July 20, 1969.""");
    }

    private static Language readLanguageToTranslateTo(final String[] args) {
        return Arrays.stream(args)
                .skip(1)
                .findFirst()
                .flatMap(Language::of)
                .orElse(Language.ITALIAN);
    }

    private Main() {}
}
