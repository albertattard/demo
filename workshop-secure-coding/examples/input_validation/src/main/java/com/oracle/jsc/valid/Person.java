package com.oracle.jsc.valid;

public record Person(String name, String city, String role, Person[] staff) {
    @Override
    public final boolean equals(Object o) {
        if (o == null || !(o instanceof Person)) {
            return false;
        }
        Person p = (Person) o;
        if (name == null) {
            return p.name == null;
        }
        return name.equals(p.name());
    }

    @Override
    public final int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    @Override
    public final String toString() {
        StringBuffer out = new StringBuffer("Person[name=");
        out.append(name());
        out.append(", city=").append(city());
        out.append(", role=").append(role());
        out.append(", staff=");
        if (staff() == null) {
            out.append("(null)");
        } else {
            out.append("[");
            boolean hasStaff = false;
            for (Person staffer : staff()) {
                hasStaff = true;
                if (staffer == null) {
                    out.append("(null), ");
                } else {
                    out.append(staffer.name()).append(", ");
                }
            }
            if (hasStaff) {
                out.delete(out.length() - 2, out.length());
            }
            out.append("]");
        }
        out.append("]");

        return out.toString();
    }
}
