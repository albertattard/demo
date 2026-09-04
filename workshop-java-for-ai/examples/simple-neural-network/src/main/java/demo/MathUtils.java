package demo;

import java.util.random.RandomGenerator;

public final class MathUtils {

    private static final RandomGenerator RANDOM = RandomGenerator.getDefault();

    /**
     * Sigmoid activation function used to introduce non-linearity. Converts real-valued inputs into the (0, 1) range.
     * <p>
     * The sigmoid function is a mathematical function that maps any real-valued input to a value between 0 and 1,
     * following an S-shaped curve. In neural networks, it's used as an activation function to introduce non-linearity
     * and is especially useful in binary classification tasks because its output can be interpreted as a probability.
     * Despite its smoothness and biological inspiration, the sigmoid has drawbacks like the vanishing gradient problem
     * and lack of zero-centered output, which can hinder training in deep networks. As a result, it's often replaced by
     * functions like ReLU or tanh in hidden layers, though it's still commonly used in output layers for binary
     * classification problems.
     *
     * @param x Input value
     * @return Sigmoid of x
     */
    public static double sigmoid(final double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    /**
     * Derivative of the sigmoid function. Assumes input x has already passed through sigmoid.
     *
     * @param x Sigmoid output
     * @return Derivative of sigmoid
     */
    public static double sigmoidDerivative(double x) {
        return x * (1 - x);
    }

    /**
     * Adds random noise to each element in the input array.
     * Useful for data augmentation to improve generalization.
     *
     * @param input Input array
     * @return New array with noise applied
     */
    public static double[] noise(final double[] input) {
        final double[] inputWithNoise = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            inputWithNoise[i] = noise(input[i]);
        }

        return inputWithNoise;
    }

    /**
     * Applies noise to a single input value.
     * Adds or subtracts a small random value based on the input size.
     *
     * @param input A value from the input
     * @return Noisy version of the input
     */
    public static double noise(final double input) {
        final double noise = RANDOM.nextDouble(0.1);
        return input < 0.1
                ? input + noise
                : input - noise;
    }

    /**
     * Returns a random double between 0.0 (inclusive) and 1.0 (exclusive)
     *
     * @return a random double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public static double nextDouble() {
        return RANDOM.nextDouble();
    }

    private MathUtils() {}
}
