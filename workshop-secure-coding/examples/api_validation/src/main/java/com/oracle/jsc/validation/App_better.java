package com.oracle.jsc.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.ConfigFileReader.ConfigFile;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * Call OCI and list the top-level compartments in your tenancy in your default
 * domain.
 */
public class App_better {
    private static Logger log = LoggerFactory.getLogger(App_better.class);

    public static void main(String[] args) throws IOException {
        String userHome = System.getProperty("user.home");
        Path configFilePath = Paths.get(userHome, "/.oci/config");

        verifyConfigFile(configFilePath);
        ConfigFile config = ConfigFileReader.parse(configFilePath.toString());
        verifyPrivateKey(config.get("key_file"));

        AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(config);

        String tenancyId = args.length == 0 ? provider.getTenantId() : args[0];

        try (IdentityClient identityClient = IdentityClient.builder().build(provider)) {
            ListCompartmentsRequest listCompartmentsRequest = ListCompartmentsRequest.builder()
                    .compartmentId(tenancyId)
                    .limit(256)
                    .accessLevel(ListCompartmentsRequest.AccessLevel.Any)
                    .compartmentIdInSubtree(false)
                    .sortBy(ListCompartmentsRequest.SortBy.Name)
                    .sortOrder(ListCompartmentsRequest.SortOrder.Asc)
                    .build();

            ListCompartmentsResponse response = identityClient.listCompartments(listCompartmentsRequest);

            int resultCode = response.get__httpStatusCode__();
            log.info("response: {}", resultCode);
            if (resultCode == 200) {
                // TODO: perform some data consistency checks on the response
                // TODO: verify the result more-or-less manages your expectations of the result
                // TODO: populate a transfer object so that OCI API values don't leak into our production code.

                log.info("Found {} compartments at the tenancy root: ", response.getItems().size());
                for (Compartment c : response.getItems()) {
                    log.info("\t{}", c.getName());
                }
            }
        }
    }

    private static void verifyPrivateKey(String keyFileName) throws IOException {
        if (keyFileName != null) {

            Set<PosixFilePermission> permissions =
                    Files.readAttributes(Paths.get(keyFileName),PosixFileAttributes.class).permissions();

            if (permissions.size() > 1 || permissions.iterator().next() != OWNER_READ) {
                throw new SecurityException(
                        "Key file " + keyFileName + " must be only readable, and only by the owner.");
            }
        }
    }

    private static void verifyConfigFile(Path configFilePath) throws IOException {
        if (!Files.exists(configFilePath))
            throw new SecurityException("Config file " + configFilePath + " is missing.");
        if (!Files.isRegularFile(configFilePath))
            throw new SecurityException("Config file " + configFilePath + " is not a plain file.");
        if (!Files.isReadable(configFilePath))
            throw new SecurityException("Config file " + configFilePath + " is not readable.");
        if (Files.size(configFilePath) > 1_000L)
            throw new SecurityException("Config file " + configFilePath + " is > 1kB.");

        PosixFileAttributes attrs = Files.readAttributes(configFilePath, PosixFileAttributes.class);
        // no write permissions or execute permissions are allowed
        Set<PosixFilePermission> permissions = attrs.permissions();
        if (permissions.contains(OTHERS_EXECUTE) || permissions.contains(OTHERS_WRITE)
                || permissions.contains(GROUP_EXECUTE) || permissions.contains(GROUP_WRITE)
                || permissions.contains(OWNER_EXECUTE) || permissions.contains(OWNER_WRITE)) {
            throw new SecurityException("Config file " + configFilePath + " is writable or executable.");
        }
    }
}
