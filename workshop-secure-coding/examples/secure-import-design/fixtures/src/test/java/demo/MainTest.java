package demo;

import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression tests supplied to attendees before they apply the safer importer.
 */
class MainTest {
    @Test
    void rejectsUnknownStaffReference() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Main.read(Path.of("fixtures", "invalid-people.json")));

        assertEquals("person #2 staff member #1 references unknown person 'Helen'.", exception.getMessage());
    }

    @Test
    void importsPeopleThatMeetTheContract() throws Exception {
        Map<String, Person> people = Main.read(Path.of("fixtures", "valid-people.json"));

        assertEquals(2, people.size());
        assertEquals("Bob", people.get("Alice").staff().getFirst().name());
    }
}
