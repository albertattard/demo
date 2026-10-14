package demo;

import demo.model.Language;
import demo.model.Missions;

import java.time.LocalDateTime;
import java.util.Optional;

public final class Main {

    public static void main(final String[] args) {
        final ModelGateway gateway = ModelGateway.create();

        summariseExample(gateway);
        translationExample(gateway);
        extractionExample(gateway);
        structuredOutputExample(gateway);
    }

    private static void summariseExample(final ModelGateway gateway) {
        System.out.println("---");
        System.out.println("Summarisation Example");

        final String text = """
                The **Apollo 11 moon landing** was a historic spaceflight mission carried out by NASA, marking \
                humanity’s first successful crewed landing on the Moon. Launched on **July 16, 1969**, from Kennedy \
                Space Center, the spacecraft carried astronauts **Neil Armstrong**, **Buzz Aldrin**, and \
                **Michael Collins**. After a three-day journey, the lunar module **Eagle** separated from the command \
                module **Columbia** and descended toward the Moon’s surface. On **July 20, 1969**, at 20:17 UTC, \
                Armstrong piloted the Eagle to a safe landing in the **Sea of Tranquility**.
                
                A few hours later, Armstrong became the first human to set foot on the lunar surface, declaring his \
                famous words: **"That's one small step for man, one giant leap for mankind."** Aldrin soon joined him, \
                and together they spent about **2.5 hours** exploring the surface, collecting rock samples, and \
                setting up experiments. Meanwhile, Collins orbited the Moon in the command module, ensuring a safe \
                return. The crew returned to Earth on **July 24, 1969**, splashing down in the Pacific Ocean. The \
                Apollo 11 mission remains one of humanity’s greatest achievements, symbolizing technological \
                innovation and the triumph of human exploration.
                """;
        System.out.println("text (" + text.length() + ")> " + text);

        final String summarised = gateway.summarise(text);
        System.out.println("summarised (" + summarised.length() + ' ' + (100 * summarised.length() / text.length()) + "%)> " + summarised);
    }

    private static void translationExample(final ModelGateway gateway) {
        System.out.println("---");
        System.out.println("Translation Example");

        final String text = """
                Apollo 11 landed on the Moon on July 20, 1969. The lunar module, nicknamed "Eagle", touched down at \
                20:17:40 UTC (4:17 PM EDT). Neil Armstrong then became the first human to walk on the Moon about six \
                and a half hours later.""";
        System.out.println("text> " + text);
        final Language language = Language.GERMAN;
        System.out.println("language> " + language);

        final String translation = gateway.translateTo(text, language);
        System.out.println("translation> " + translation);
    }

    private static void extractionExample(final ModelGateway gateway) {
        System.out.println("---");
        System.out.println("Extraction (date/time) Example");

        final String text = """
                Apollo 11 landed on the Moon on July 20, 1969. The lunar module, nicknamed "Eagle", touched down at \
                20:17:40 UTC (4:17 PM EDT). Neil Armstrong then became the first human to walk on the Moon about six \
                and a half hours later.""";
        System.out.println("text> " + text);

        final Optional<LocalDateTime> dateTime = gateway.extractDateTimeFrom(text);
        System.out.println("dateTime> " + dateTime.map(Object::toString)
                .orElse("Failed to extract the date and time"));
    }

    private static void structuredOutputExample(final ModelGateway gateway) {
        System.out.println("---");
        System.out.println("Structured Output Extraction Example");

        final Missions missions = gateway.lunarMissionsOn(1969);
        System.out.println("There were " + missions.size() + " missions in " + missions.year());
        missions.forEach(mission -> System.out.println(" - " + mission));
        System.out.println("The model can make mistakes. Please verify (https://en.wikipedia.org/wiki/List_of_missions_to_the_Moon)!.");
    }

    private Main() {}
}
