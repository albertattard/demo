package demo;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Safer importer used as the workshop fixture. Validation belongs at the file-import
 * trust boundary, before Person objects are made available to the rest of the program.
 */
public final class Main {
    private static final long MAX_INPUT_BYTES = 1_048_576;
    private static final int MAX_PEOPLE = 1_000;
    private static final int MAX_TEXT_LENGTH = 100;

    static void main(final String[] args) {
        Path input = args.length == 0 ? Path.of("test.json") : Path.of(args[0]);
        try {
            emit(read(input));
        } catch (IOException | ParseException | IllegalArgumentException exception) {
            System.err.println("Input rejected: " + exception.getMessage());
            System.exit(10);
        }
    }

    public static Map<String, Person> read(Path input) throws IOException, ParseException {
        validateFile(input);

        Object rawData;
        try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            rawData = new JSONParser().parse(reader);
        }

        if (!(rawData instanceof JSONArray data)) {
            throw rejected("the top-level value must be an array.");
        }
        if (data.size() > MAX_PEOPLE) {
            throw rejected("the file contains more than " + MAX_PEOPLE + " people.");
        }

        Map<String, PendingPerson> pendingPeople = parsePeople(data);
        return resolveStaff(pendingPeople);
    }

    private static void validateFile(Path input) throws IOException {
        if (!Files.isRegularFile(input) || !Files.isReadable(input)) {
            throw rejected("'" + input + "' must be a readable regular file.");
        }
        if (Files.size(input) > MAX_INPUT_BYTES) {
            throw rejected("'" + input + "' is larger than " + MAX_INPUT_BYTES + " bytes.");
        }
    }

    private static Map<String, PendingPerson> parsePeople(JSONArray data) {
        Map<String, PendingPerson> people = new LinkedHashMap<>();
        for (int index = 0; index < data.size(); index++) {
            Object value = data.get(index);
            int personNumber = index + 1;
            if (!(value instanceof JSONObject object)) {
                throw rejected("person #" + personNumber + " must be an object.");
            }

            String name = requiredText(object, "name", personNumber);
            String city = requiredText(object, "city", personNumber);
            String role = requiredText(object, "role", personNumber);
            List<String> staffNames = staffNames(object, personNumber);
            if (people.putIfAbsent(name, new PendingPerson(name, city, role, staffNames)) != null) {
                throw rejected("person #" + personNumber + " duplicates the name '" + name + "'.");
            }
        }
        return people;
    }

    private static String requiredText(JSONObject object, String field, int personNumber) {
        Object value = object.get(field);
        if (!(value instanceof String text) || text.isBlank() || text.length() > MAX_TEXT_LENGTH) {
            throw rejected("person #" + personNumber + " field '" + field
                    + "' must be non-blank text of at most " + MAX_TEXT_LENGTH + " characters.");
        }
        return text;
    }

    private static List<String> staffNames(JSONObject object, int personNumber) {
        Object value = object.get("staff");
        if (!(value instanceof JSONArray staff)) {
            throw rejected("person #" + personNumber + " field 'staff' must be an array of names.");
        }

        List<String> names = new ArrayList<>();
        for (int index = 0; index < staff.size(); index++) {
            Object staffer = staff.get(index);
            if (!(staffer instanceof String name) || name.isBlank() || name.length() > MAX_TEXT_LENGTH) {
                throw rejected("person #" + personNumber + " staff member #" + (index + 1)
                        + " must be non-blank text of at most " + MAX_TEXT_LENGTH + " characters.");
            }
            names.add(name);
        }
        return names;
    }

    private static Map<String, Person> resolveStaff(Map<String, PendingPerson> pendingPeople) {
        Map<String, Person> people = new LinkedHashMap<>();
        for (PendingPerson pending : pendingPeople.values()) {
            people.put(pending.name(), new Person(pending.name(), pending.city(), pending.role(), List.of()));
        }

        int personNumber = 0;
        for (PendingPerson pending : pendingPeople.values()) {
            personNumber++;
            List<Person> staff = new ArrayList<>();
            int staffNumber = 0;
            for (String staffName : pending.staffNames()) {
                staffNumber++;
                Person staffer = people.get(staffName);
                if (staffer == null) {
                    throw rejected("person #" + personNumber + " staff member #" + staffNumber
                            + " references unknown person '" + staffName + "'.");
                }
                staff.add(staffer);
            }
            people.put(pending.name(), new Person(pending.name(), pending.city(), pending.role(), staff));
        }
        return people;
    }

    private static IllegalArgumentException rejected(String message) {
        return new IllegalArgumentException(message);
    }

    private static void emit(Map<String, Person> people) {
        people.values().forEach(System.out::println);
    }

    private record PendingPerson(String name, String city, String role, List<String> staffNames) {
    }
}
