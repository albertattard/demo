package demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class Neuron implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Parameters parameters;
    private final ActivationFunction activationFunction;

    /* Needed for training (backpropagation) */
    private transient double[] lastInput;
    private transient double lastOutput;
    private transient double lastDelta;

    public static Neuron create(final int inputDimension) {
        return create(inputDimension, ActivationFunction.SIGMOID);
    }

    public static Neuron create(final int inputDimension, final ActivationFunction activationFunction) {
        if (inputDimension < 1) {
            throw new IllegalArgumentException("The neuron's input dimension cannot be less than 1");
        }
        requireNonNull(activationFunction);

        return create(Parameters.create(inputDimension), activationFunction);
    }

    public static Neuron create(final Parameters parameters) {
        return create(parameters, ActivationFunction.SIGMOID);
    }

    public static Neuron create(final Parameters parameters, final ActivationFunction activationFunction) {
        requireNonNull(parameters);
        requireNonNull(activationFunction);

        return new Neuron(parameters, activationFunction);
    }

    private Neuron(final Parameters parameters, final ActivationFunction activationFunction) {
        this.parameters = parameters;
        this.lastInput = new double[parameters.inputDimension()];
        this.activationFunction = activationFunction;
    }

    public double compute(final double[] input) {
        requireNonNull(input);
        if (inputDimension() != input.length) {
            throw new IllegalArgumentException("Expected input dimension of " + inputDimension() + " but provided " + input.length);
        }

        System.arraycopy(input, 0, this.lastInput, 0, input.length);
        double sum = parameters.bias();
        for (int i = 0; i < inputDimension(); i++) {
            sum += parameters.weightAt(i) * input[i];
        }
        this.lastOutput = activationFunction.compute(sum);
        return this.lastOutput;
    }

    /* For output layer neurons: compute delta and update weights */
    public void backpropagateOutput(final double target, final double learningRate) {
        this.lastDelta = (lastOutput - target) * activationFunction.derivative(lastOutput);
        updateWeights(learningRate);
    }

    /* For hidden layer neurons: receive weighted sum of next layer's deltas */
    public void backpropagateHidden(final double downstreamWeightedDelta, final double learningRate) {
        this.lastDelta = downstreamWeightedDelta * activationFunction.derivative(lastOutput);
        updateWeights(learningRate);
    }

    private void updateWeights(final double learningRate) {
        final double learningRateDelta = learningRate * lastDelta;
        for (int i = 0; i < inputDimension(); i++) {
            parameters.updateWeightAt(i, -(learningRateDelta * lastInput[i]));
        }
        parameters.updateBias(-learningRateDelta);
    }

    public double weightLastDeltaAt(final int index) {
        return parameters.weightAt(index) * lastDelta;
    }

    public int inputDimension() {
        return parameters.inputDimension();
    }

    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        /* This field is transient and thus never initialised during the deserialisation */
        this.lastInput = new double[parameters.inputDimension()];
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final Neuron other
               && Objects.equals(parameters, other.parameters)
               && activationFunction == other.activationFunction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, activationFunction);
    }

    @Override
    public String toString() {
        return "Neuron[parameters=" + parameters + ", activationFunction=" + activationFunction + ']';
    }
}
