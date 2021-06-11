package de.fraunhofer.isst.dataspaceconnector.camel.config.context;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.camel.CamelContext;
import org.apache.camel.component.http.HttpComponent;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

/**
 * Configures the Camel HTTP component.
 */
@Configuration
public class CamelHttpConfig {

    /**
     * Location of the truststore to use.
     */
    @Value("${http.truststore.path}")
    private String trustStoreLocation;

    /**
     * Password of the truststore to use.
     */
    @Value("${http.truststore.password}")
    private String trustStorePassword;

    /**
     * The Camel context.
     */
    private final CamelContext camelContext;

    /**
     * Constructs a CamelHttpConfig object using the given CamelContext.
     *
     * @param camelContext the Camel context.
     */
    @Autowired
    public CamelHttpConfig(final CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    /**
     * Creates a custom {@link SSLContext} using the truststore defined in application.properties
     * and configures the Camel HTTP component from the Camel context to use this custom SSL
     * context.
     *
     * @throws IOException if an error occurs while loading the truststore.
     * @throws CertificateException if an error occurs while loading the truststore.
     * @throws NoSuchAlgorithmException if an error occurs while loading the truststore.
     * @throws KeyStoreException if an error occurs while loading the truststore.
     * @throws KeyManagementException if the SSL context can not be built.
     */
    @PostConstruct
    public void configureTruststore() throws IOException, CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final var sslContext = SSLContexts
                .custom()
                .loadTrustMaterial(ResourceUtils.getFile(trustStoreLocation),
                        trustStorePassword.toCharArray())
                .build();

        final var socketFactory = new SSLConnectionSocketFactory(sslContext);

        final var socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory> create().register("https", socketFactory)
                .build();

        final var connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        final var httpComponent = camelContext.getComponent("http", HttpComponent.class);
        httpComponent.setClientConnectionManager(connectionManager);

        final var httpsComponent = camelContext.getComponent("https", HttpComponent.class);
        httpsComponent.setClientConnectionManager(connectionManager);
    }

}
