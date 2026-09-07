package demo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Input validation demo
 */
public final class Main {

    static void main(final String[] args) throws IOException, ParseException {
        final Path input = args.length == 0
                ? Path.of("test.json")
                : Path.of(args[0]);

        final Map<String, Person> personsByName = read(input);
        personsByName.values().forEach(System.out::println);
    }

    /**
     * Deliberately unsafe importer used as the vulnerable baseline for the workshop.
     * It parses an untrusted file but assumes its structure and relationships are valid.
     */
    public static Map<String, Person> read(final Path input) throws IOException, ParseException {
        final JSONParser parser = new JSONParser();
        final Map<String, Person> personsByName = new LinkedHashMap<>();

        final JSONArray data;
        try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            data = (JSONArray) parser.parse(reader);
        }

        // build basic people
        for (final Object datum : data) {
            final JSONObject object = (JSONObject) datum;
            final String name = (String) object.get("name");
            final String city = (String) object.get("city");
            final String role = (String) object.get("role");
            personsByName.put(name, new Person(name, city, role, null));
        }

        // map staff
        for (final Object datum : data) {
            final JSONObject object = (JSONObject) datum;
            final JSONArray staffArray = (JSONArray) object.get("staff");

            if (staffArray != null) {
                final Person person = personsByName.get(object.get("name"));

                final List<Person> staff = new ArrayList<>();
                for (final Object o : staffArray) {
                    final String stafferName = (String) o;
                    staff.add(personsByName.get(stafferName));
                }

                personsByName.put(person.name(), new Person(person.name(), person.city(), person.role(), staff));
            }
        }

        return personsByName;
    }
}
