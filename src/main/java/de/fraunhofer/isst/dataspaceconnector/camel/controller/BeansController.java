package de.fraunhofer.isst.dataspaceconnector.camel.controller;

import java.io.IOException;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
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
@RequestMapping("/beans")
public class BeansController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeansController.class);

    /**
     * The application context.
     */
    private final GenericApplicationContext applicationContext;

    @Autowired
    public BeansController(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Adds one or more beans from an XML file to the application context.
     *
     * @param file the XML file
     * @return a response entity with code 200 or 500, if an error occurs
     */
    @PostMapping
    public ResponseEntity<String> addBeans(@RequestParam("file") MultipartFile file) {
        try {
            String xml = new String(file.getBytes());
            XmlBeanDefinitionReader xmlBeanDefinitionReader =
                    new XmlBeanDefinitionReader(applicationContext);
            xmlBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
            xmlBeanDefinitionReader.loadBeanDefinitions(new InputSource(new StringReader(xml)));

            return new ResponseEntity<>("Successfully added beans to Application Context.",
                    HttpStatus.OK);
        } catch (IOException | BeanDefinitionStoreException e) {
            LOGGER.error("Could not read or add bean(s) from XML file. [exception=({})]",
                    e.getMessage());
            return new ResponseEntity<>("Could not add beans to Application Context: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a bean from the application context by its ID.
     *
     * @param beanId the bean ID
     * @return a response entity with code 200 or 500, if an error occurs
     */
    @DeleteMapping("/{beanId}")
    public ResponseEntity<String> removeBean(@PathVariable("beanId") String beanId) {
        try {
            BeanDefinitionRegistry registry =
                    (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
            registry.removeBeanDefinition(beanId);

            return new ResponseEntity<>("Successfully removed bean with ID " + beanId,
                    HttpStatus.OK);
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.error("Could not remove bean with ID {} from application context. "
                    + "[exception=({})]", beanId, e.getMessage());
            return new ResponseEntity<>("Could not remove bean: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
