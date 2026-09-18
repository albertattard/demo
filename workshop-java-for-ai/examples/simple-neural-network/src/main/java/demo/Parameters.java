package demo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static demo.MathUtils.nextDouble;
import static java.util.Objects.requireNonNull;

public final class Parameters implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final double[] weights;
    private double bias;

    public static Parameters create(final double[] values, final int offset, final int inputDimension) {
        requireNonNull(values);
        if (offset < 0 || offset >= values.length) {
            throw new IllegalArgumentException("The values' offset must be between 0 (inclusive) and the values's length (exclusive)");
        }
        if (inputDimension < 1) {
            throw new IllegalArgumentException("The neuron's input dimension cannot be less than 1");
        }

        final Parameters parameters = new Parameters(inputDimension);
        System.arraycopy(values, offset, parameters.weights, 0, inputDimension);
        parameters.bias = values[offset + inputDimension];
        return parameters;
    }

    public static Parameters create(final int inputDimension) {
        if (inputDimension < 1) {
            throw new IllegalArgumentException("The neuron's input dimension cannot be less than 1");
        }

        return new Parameters(inputDimension)
                .init();
    }

    private Parameters(final int inputDimension) {
        weights = new double[inputDimension];
    }

    private Parameters init() {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = nextDouble() - 0.5;
        }
        bias = nextDouble() - 0.5;

        return this;
    }

    public int inputDimension() {
        return weights.length;
    }

    public double weightAt(final int index) {
        return weights[index];
    }

    public double bias() {
        return bias;
    }

    public void updateWeightAt(final int index, final double adjustment) {
        weights[index] += adjustment;
    }

    public void updateBias(final double adjustment) {
        bias += adjustment;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final Parameters other
               && Double.compare(bias, other.bias) == 0
               && Arrays.equals(weights, other.weights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(weights), bias);
    }

    @Override
    public String toString() {
        return "Parameters[weights=" + Arrays.toString(weights) + ", bias=" + bias + ']';
    }
}
