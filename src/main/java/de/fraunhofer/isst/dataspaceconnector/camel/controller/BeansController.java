package de.fraunhofer.isst.dataspaceconnector.camel.controller;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;

/**
 * Controller for adding and removing beans at runtime.
 */
@RestController
@RequestMapping("/api/beans")
public class BeansController {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BeansController.class);

    /**
     * Reader for parsing beans from XML and automatically adding them to application context.
     */
    private final XmlBeanDefinitionReader xmlBeanReader;

    /**
     * Bean registry of the application context.
     */
    private final BeanDefinitionRegistry beanRegistry;

    /**
     * Constructor for the BeansController.
     *
     * @param xmlBeanReader the XmlBeanDefinitionReader.
     * @param beanRegistry the BeanDefinitionRegistry.
     */
    @Autowired
    public BeansController(final XmlBeanDefinitionReader xmlBeanReader,
                           final BeanDefinitionRegistry beanRegistry) {
        this.xmlBeanReader = xmlBeanReader;
        this.beanRegistry = beanRegistry;
    }

    /**
     * Adds one or more beans from an XML file to the application context.
     *
     * @param file the XML file.
     * @return a response entity with code 200 or 500, if an error occurs.
     */
    @PostMapping
    public ResponseEntity<String> addBeans(@RequestParam("file") final MultipartFile file) {
        try {
            if (file == null) {
                throw new IllegalArgumentException("File must not be null");
            }

            final var xml = new String(file.getBytes(), StandardCharsets.UTF_8);
            final var numberOfBeans = xmlBeanReader
                    .loadBeanDefinitions(new InputSource(new StringReader(xml)));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Added {} beans to the Application Context.", numberOfBeans);
            }

            return new ResponseEntity<>("Successfully added " + numberOfBeans
                    + " beans to Application Context.", HttpStatus.OK);
        } catch (IllegalArgumentException | IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read XML file because file was null.");
            }
            return new ResponseEntity<>("File must not be null.", HttpStatus.BAD_REQUEST);
        } catch (BeanDefinitionStoreException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read bean(s) from XML file. [exception=({})]",
                        e.getMessage(), e);
            }
            return new ResponseEntity<>("Could not add beans to Application Context: "
                    + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes a bean from the application context by its ID.
     *
     * @param beanId the bean ID.
     * @return a response entity with code 200 or 500, if an error occurs.
     */
    @DeleteMapping("/{beanId}")
    public ResponseEntity<String> removeBean(@PathVariable("beanId") final String beanId) {
        try {
            beanRegistry.removeBeanDefinition(beanId);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Removed bean with ID {} from the Application Context.", beanId);
            }

            return new ResponseEntity<>("Successfully removed bean with ID " + beanId,
                    HttpStatus.OK);
        } catch (NoSuchBeanDefinitionException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not remove bean with ID {} from application context. "
                        + "[exception=({})]", beanId, e.getMessage(), e);
            }
            return new ResponseEntity<>("No bean found with ID " + beanId,
                    HttpStatus.BAD_REQUEST);
        }
    }

}
