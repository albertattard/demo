package demo;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very trusting trust manager--one that trusts all certs and cert chains.
 */
public class VeryTrustingTrustManager implements X509TrustManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        logger.info("checkClientTrusted(array of {} certs, authtype: {})", chain.length, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        logger.info("checkServerTrusted(array of {} certs, authtype: {})", chain.length, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        logger.info("getAcceptedIssuers(); returning empty array.");
        return new X509Certificate[0];
    }
}
