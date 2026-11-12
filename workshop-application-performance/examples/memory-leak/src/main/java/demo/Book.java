package demo;

import java.util.Objects;

public final class Book {

    private final String name;

    public Book(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final Book other
                && Objects.equals(name, other.name);
    }
}
