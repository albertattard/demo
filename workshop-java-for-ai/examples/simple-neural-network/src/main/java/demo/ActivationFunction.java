package demo;

import java.io.Serializable;

public enum ActivationFunction implements Serializable {

    SIGMOID;

    public double compute(final double value) {
        return switch (this) {
            case SIGMOID -> 1.0 / (1.0 + Math.exp(-value));
        };
    }

    public double derivative(final double output) {
        return switch (this) {
            case SIGMOID -> output * (1.0 - output);
        };
    }
}
