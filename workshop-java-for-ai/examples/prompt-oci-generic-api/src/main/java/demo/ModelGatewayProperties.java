package demo;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

record ModelGatewayProperties(
        String region,
        String compartmentOcid,
        String modelOcid,
        Configuration configuration) {

    ModelGatewayProperties {
        requireNonNull(region, "region is required");
        requireNonNull(compartmentOcid, "compartment OCID is required");
        requireNonNull(modelOcid, "model OCID is required");
        requireNonNull(configuration, "configuration is required");
    }

    static ModelGatewayProperties load() {
        final Path path = findPropertiesFileInWellKnownLocations()
                .orElseThrow(() -> new RuntimeException("The application.properties is missing"));

        try (Reader reader = Files.newBufferedReader(path)) {
            return load(reader);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to load properties from " + path, e);
        }
    }

    private static ModelGatewayProperties load(final Reader reader) {
        final Properties properties = new Properties();
        try {
            properties.load(reader);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to load properties", e);
        }

        final Configuration configuration = Configuration.of(properties);

        return new ModelGatewayProperties(
                properties.getProperty("model.gateway.region"),
                properties.getProperty("model.gateway.compartmentOcid"),
                properties.getProperty("model.gateway.modelOcid"),
                configuration);
    }

    private static Optional<Path> findPropertiesFileInWellKnownLocations() {
        return Stream.of(Path.of("application.properties"),
                        Path.of(System.getProperty("user.home"), ".oci", "application.properties"))
                .filter(Files::isRegularFile)
                .findFirst();
    }

    sealed interface Configuration {
        static Configuration of(final Properties properties) {
            final String type = properties.getProperty("model.gateway.oci.configuration.type", "FILE");

            return switch (type.toUpperCase()) {
                case "FILE" -> new FileBasedConfiguration(
                        properties.getProperty("model.gateway.oci.configuration.file", "~/.oci/config"),
                        properties.getProperty("model.gateway.oci.configuration.profile", "DEFAULT")
                );
                default -> throw new IllegalArgumentException("Unsupported configuration type " + type);
            };
        }
    }

    record FileBasedConfiguration(String file, String profile) implements Configuration {}
}
