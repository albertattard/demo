package demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record Person(String name, String city, String role, List<Person> staff) {

    public Person {
        // The vulnerable importer deliberately demonstrates an unresolved null staffer.
        // Copy without allowing callers to mutate the list while preserving that state.
        staff = staff == null ? null : Collections.unmodifiableList(new ArrayList<>(staff));
    }

    @Override
    public boolean equals(final Object object) {
        // The safer importer validates name as this fixture's unique person identifier.
        return object instanceof final Person other
                && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        // Avoid the record-generated toString: staff is a graph that may contain cycles.
        return "Person[name=" + name + ", city=" + city + ", role=" + role + ", staff="
                + (staff == null ? "(null)" : staff.stream()
                .map(person -> person == null ? "(null)" : person.name())
                .toList()) + "]";
    }
}
