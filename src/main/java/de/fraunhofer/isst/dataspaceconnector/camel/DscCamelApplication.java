package de.fraunhofer.isst.dataspaceconnector.camel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class },
        scanBasePackages = {"de.fraunhofer.isst.dataspaceconnector.camel"})
//@ImportResource({"file:/camel/camel*.xml"})
@ImportResource("classpath:camel-context.xml")
public class DscCamelApplication {

    public static void main(String[] args) {
        SpringApplication.run(DscCamelApplication.class, args);
    }

}
