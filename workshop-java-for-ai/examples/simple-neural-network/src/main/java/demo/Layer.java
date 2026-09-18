package demo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public final class Layer implements Serializable  {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Neuron[] neurons;

    public static Layer create(final Parameters[] parameters) {
        requireNonNull(parameters);

        final Neuron[] neurons = Arrays.stream(parameters)
                .map(Neuron::create)
                .toArray(Neuron[]::new);
        return new Layer(neurons);
    }

    public static Layer create(final int inputDimension) {
        return create(inputDimension, inputDimension);
    }

    public static Layer create(final int width, final int inputDimension) {
        if (width < 1) {throw new IllegalArgumentException("The layer's width cannot be less than 1");}
        if (inputDimension < 1) {
            throw new IllegalArgumentException("The layer's neuron's input dimension cannot be less than 1");
        }

        final Neuron[] neurons = new Neuron[width];
        for (int i = 0; i < width; i++) {
            neurons[i] = Neuron.create(inputDimension);
        }
        return new Layer(neurons);
    }

    private Layer(final Neuron[] neurons) {
        this.neurons = neurons;
    }

    public int inputDimension() {
        return neurons[0].inputDimension();
    }

    public double[] compute(final double[] input) {
        requireNonNull(input);

        final double[] output = new double[this.neurons.length];

        /* TODO: this can be parallelized, because each computation is independent */
        for (int i = 0; i < this.neurons.length; i++) {
            output[i] = this.neurons[i].compute(input);
        }

        return output;
    }

    /* For output layer – expected is the ground truth */
    public void backpropagateOutput(final double[] expected, final double learningRate) {
        requireNonNull(expected);
        if (expected.length != neurons.length) {
            throw new IllegalArgumentException("Expected output length must match number of neurons.");
        }

        for (int i = 0; i < neurons.length; i++) {
            neurons[i].backpropagateOutput(expected[i], learningRate);
        }
    }

    /* For hidden layer – uses deltas from next layer */
    public void backpropagateHidden(final Layer nextLayer, final double learningRate) {
        requireNonNull(nextLayer);

        for (int i = 0; i < neurons.length; i++) {
            double sum = 0;
            for (final Neuron neuron : nextLayer.neurons) {
                sum += neuron.weightLastDeltaAt(i);
            }

            neurons[i].backpropagateHidden(sum, learningRate);
        }
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final Layer layer
               && Arrays.equals(neurons, layer.neurons);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(neurons);
    }

    @Override
    public String toString() {
        return "Layer[neurons=" + Arrays.toString(neurons) + ']';
    }
}
