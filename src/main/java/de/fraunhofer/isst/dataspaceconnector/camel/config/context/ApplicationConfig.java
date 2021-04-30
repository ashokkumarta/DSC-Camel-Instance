package de.fraunhofer.isst.dataspaceconnector.camel.config.context;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import lombok.NoArgsConstructor;
import org.apache.camel.model.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Contains beans required for reading, deploying and/or removing routes and beans.
 */
@Configuration
@NoArgsConstructor
public class ApplicationConfig {

    /**
     * Creates an Unmarshaller bean that can be used to read bean definitions from XML.
     *
     * @return the Unmarshaller
     */
    @Bean
    public Unmarshaller unmarshaller() {
        try {
            final var jaxb = JAXBContext.newInstance(Constants.JAXB_CONTEXT_PACKAGES);
            return jaxb.createUnmarshaller();
        } catch (JAXBException e) {
            throw new BeanCreationException("Failed to create Unmarshaller", e);
        }
    }

    /**
     * Creates and configures an XmlBeanDefinitionReader bean, that reads beans from XML and
     * automatically adds them to the application context.
     *
     * @param appContext the application context
     * @return the XmlBeanDefinitionReader
     */
    @Bean
    public XmlBeanDefinitionReader xmlBeanDefinitionReader(final GenericApplicationContext
                                                                       appContext) {
        final var xmlBeanReader = new XmlBeanDefinitionReader(appContext);
        xmlBeanReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
        return xmlBeanReader;
    }

    /**
     * Creates the application context's BeanDefinitionRegistry as a bean, which can be used to
     * remove beans from the context.
     *
     * @param appContext the application context
     * @return the BeanDefinitionRegistry
     */
    @Bean
    public BeanDefinitionRegistry beanDefinitionRegistry(final GenericApplicationContext
                                                                     appContext) {
        return (BeanDefinitionRegistry) appContext.getAutowireCapableBeanFactory();
    }

    /**
     * Creates the Camel route Logger as a bean so that it can be used in routes (for printing
     * messages set in log tag).
     *
     * @return the Logger for Camel routes
     */
    @Bean("route-logger")
    public Logger getCamelRouteLogger() {
        return LoggerFactory.getLogger("camel-route-logger");
    }

}
