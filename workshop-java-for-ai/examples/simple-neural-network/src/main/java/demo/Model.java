package demo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class Model implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Layer[] layers;

    public static Model readFrom(final Path path) {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            return readFrom(inputStream);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read from path " + path, e);
        }
    }

    public static Model readFrom(final InputStream inputStream) {
        try {
            final ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(inputStream));
            return (Model) in.readObject();
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read from input stream", e);
        } catch (final ClassNotFoundException | RuntimeException e) {
            throw new RuntimeException("Failed to parse model", e);
        }
    }

    public static Model createWithoutHiddenLayers(final int inputDimension, final int outputDimension) {
        if (inputDimension < 1) {
            throw new IllegalArgumentException("The model's input dimension cannot be less than 1");
        }

        if (outputDimension < 1) {
            throw new IllegalArgumentException("The model's output dimension cannot be less than 1");
        }


        final Layer inputLayer = Layer.create(inputDimension);
        final Layer outputLayer = Layer.create(outputDimension, inputDimension);
        final Layer[] layers = {inputLayer, outputLayer};
        return new Model(layers);
    }

    private Model(final Layer[] layers) {
        this.layers = layers;
    }

    public void train(final double[] input, final double[] expected, final double learningRate) {
        // 1. Forward pass
        predict(input);

        // 2. Backward pass (start from output layer)
        for (int i = layers.length - 1; i >= 0; i--) {
            if (i == layers.length - 1) {
                // Output layer
                layers[i].backpropagateOutput(expected, learningRate);
            } else {
                // Hidden layer(s)
                layers[i].backpropagateHidden(layers[i + 1], learningRate);
            }
        }
    }

    public int classify(final double[] input) {
        final double[] output = predict(input);
        int maxIndex = 0;
        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public double[] predict(final double[] input) {
        double[] output = input;
        for (final Layer layer : layers) {
            output = layer.compute(output);
        }
        return output;
    }

    public void writeTo(final Path path) {
        createParentDirectoryIfMissing(path);

        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            writeTo(outputStream);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to write to output stream", e);
        }
    }

    public void writeTo(final OutputStream outputStream) {
        try {
            final ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(this);
            out.flush();
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to write to output stream", e);
        }
    }

    private static void createParentDirectoryIfMissing(final Path path) {
        final Path parent = path.toAbsolutePath().getParent();
        if (!Files.isDirectory(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (final IOException e) {
                throw new UncheckedIOException("Failed to create missing parent directories: " + parent, e);
            }
        }
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof final Model other
               && Arrays.equals(layers, other.layers);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(layers);
    }

    @Override
    public String toString() {
        return "Model[layers=" + Arrays.toString(layers) + ']';
    }
}
