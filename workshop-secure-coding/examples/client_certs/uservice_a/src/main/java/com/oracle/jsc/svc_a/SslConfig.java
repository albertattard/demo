package com.oracle.jsc.svc_a;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

/**
 * Build an SSL context and associated web client that can provide a certificate
 * for identification when making an outbound call.
 * 
 * See <a href= "https://medium.com/geekculture/authentication-using-certificates-7e2cfaacd18b">
 *      https://medium.com/geekculture/authentication-using-certificates-7e2cfaacd18b</a>.
 */
@Component
public class SslConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${client-a.ssl.trust-store}")
    private String trustStorePath;

    @Value("${client-a.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${client-a.ssl.key-store}")
    private String keyStorePath;

    @Value("${client-a.ssl.key-store-password}")
    private String keyStorePassword;

    /**
     * Build the SslContext which holds the client KeyStore and TrustStore to be used during client/server cert auth SSL handshake
     * 
     * @return SslContext - to be used by the {@link this#webClient()}
     */
    public SslContext buildSslContextForReactorClientHttpConnector() {
        SslContext sslContext = null;
        try (FileInputStream keyStoreFileInputStream = new FileInputStream(ResourceUtils.getFile(keyStorePath));
                FileInputStream trustStoreFileInputStream = new FileInputStream(ResourceUtils.getFile(trustStorePath));) {
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(keyStoreFileInputStream, keyStorePassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            KeyStore trustStore = KeyStore.getInstance("jks");
            trustStore.load(trustStoreFileInputStream, trustStorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            sslContext = SslContextBuilder
                    .forClient()
                    .keyManager(keyManagerFactory)
                    .trustManager(trustManagerFactory)
                    .build();
        } catch (Exception exception) {
            log.error("Exception while building SSL context for reactor web client: ", exception);
        }

        return sslContext;
    }

    /**
     * Bean to be used while making an REST call to the server.
     * 
     * This bean has the {@link this#buildSslContextForReactorClientHttpConnector()} keys and
     * certificate that are exchanged during the handshake
     * 
     * @return webClient - to be wired where the server call expects a 2-way SSL handshake
     */
    @Bean
    public WebClient webClient() {
        SslProvider sslProvider = SslProvider
                .builder()
                .sslContext(buildSslContextForReactorClientHttpConnector())
                .build();

        HttpClient httpClient = HttpClient.create().secure(sslProvider);

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
