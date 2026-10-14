package demo;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public final class Main {

    public static void main(final String[] args) {
        final String prompt = readPromptFromArgs(args);
        System.out.println("prompt> " + prompt);

        /* Prevents the main thread from exiting before the model finishes to reply */
        final CountDownLatch waitUntilCompletion = new CountDownLatch(1);

        System.out.print("assistant> ");
        final ModelGateway gateway = ModelGateway.create();
        gateway.prompt(prompt, new ModelGateway.PromptCallback() {
            @Override
            public void onToken(final String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete(final String response) {
                waitUntilCompletion.countDown();
            }

            @Override
            public void onError(final Throwable throwable) {
                throw new RuntimeException("Failed to prompt the model", throwable);
            }
        });

        try {
            waitUntilCompletion.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the model to complete", e);
        }
    }

    private static String readPromptFromArgs(final String[] args) {
        return Arrays.stream(args)
                .findFirst()
                .orElse("When did humans first land on the Moon?");
    }

    private Main() {}
}
