package com.oracle.jsc.obfuscated;
import static java.lang.Character.getNumericValue;

public final class BigCharacters {

    public static void main(final String...a‮) {
        for (char c‮ = 1; c‮ > 0; c‮++)
            if (getNumericValue(c‮) > 50)
                System.out.println(c‮ + ": " + getNumericValue(c‮));
    }
}