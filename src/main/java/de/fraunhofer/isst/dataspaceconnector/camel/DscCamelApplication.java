package de.fraunhofer.isst.dataspaceconnector.camel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * The application's main class.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class},
        scanBasePackages = {"de.fraunhofer.isst.dataspaceconnector.camel"})
//@ImportResource("classpath:camel-context.xml")
public class DscCamelApplication {

    /**
     * The main method that starts the application.
     * @param args list of arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(DscCamelApplication.class, args);
    }

}
