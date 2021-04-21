package de.fraunhofer.isst.dataspaceconnector.camel.util;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

import lombok.NoArgsConstructor;
import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HttpClientConfigurer that can be used in routes when an HTTP endpoint that uses a self-signed
 * certificate is called. This should only be used in test environments as it disables the hostname
 * verification!
 */
@NoArgsConstructor
public class SelfSignedHttpClientConfigurer implements HttpClientConfigurer {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SelfSignedHttpClientConfigurer.class);

    /**
     * Configures the HTTP client to be able to work with self-signed certificates by disabling
     * the hostname verification.
     *
     * @param httpClientBuilder the HttpClientBuilder.
     */
    @Override
    public void configureHttpClient(final HttpClientBuilder httpClientBuilder) {
        try {
            final SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (x509CertChain, authType) -> true).build();
            httpClientBuilder.setSSLContext(sslContext)
                    .setConnectionManager(new PoolingHttpClientConnectionManager(RegistryBuilder
                            .<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.INSTANCE)
                            .register("https", new SSLConnectionSocketFactory(sslContext,
                                    NoopHostnameVerifier.INSTANCE))
                            .build()));
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unable to create HttpClientConfigurer for self-signed certificates. "
                        + "[exception=({})]", e.getMessage(), e);
            }
        }
    }

}
