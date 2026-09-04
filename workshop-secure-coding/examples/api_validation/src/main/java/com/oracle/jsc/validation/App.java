package com.oracle.jsc.validation;

import java.io.IOException;

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

/**
 * Call OCI and list the top-level compartments in your tenancy in your default domain.
 */
public class App {
    private static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        ConfigFile config = ConfigFileReader.parseDefault();
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
                log.info("Found {} compartments at the tenancy root: ", response.getItems().size());
                for (Compartment c : response.getItems()) {
                    log.info("\t{}", c.getName());
                }
            }
        }
    }
}
