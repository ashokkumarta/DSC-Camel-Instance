package de.fraunhofer.isst.dataspaceconnector.camel.controller;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.Constants;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for adding and removing routes at runtime.
 */
@RestController
@RequestMapping("/routes")
public class RoutesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesController.class);

    /**
     * The Camel context.
     */
    private final DefaultCamelContext camelContext;

    @Autowired
    public RoutesController(CamelContext camelContext) {
        this.camelContext = (DefaultCamelContext) camelContext;
    }

    /**
     * Adds one or more routes from an XML file to the Camel context.
     *
     * @param file the XML file
     * @return a response entity with code 200 or 500, if an error occurs
     */
    @PostMapping
    public ResponseEntity<String> addRoute(@RequestParam("file") MultipartFile file) {
        try {
            JAXBContext jaxb = JAXBContext.newInstance(Constants.JAXB_CONTEXT_PACKAGES);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();

            InputStream inputStream = file.getInputStream();
            RoutesDefinition routes = (RoutesDefinition) unmarshaller.unmarshal(inputStream);
            camelContext.addRouteDefinitions(routes.getRoutes());

            return new ResponseEntity<>("Successfully added routes to Camel Context.",
                    HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Could not read or add route(s) from XML file. [exception=({})]",
                    e.getMessage());
            return new ResponseEntity<>("Could not add route(s) to Camel Context: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a route from the Camel context by its ID.
     *
     * @param routeId the route ID
     * @return a response entity with code 200 or 500, if an error occurs
     */
    @DeleteMapping("/{routeId}")
    public ResponseEntity<String> removeRoute(@PathVariable("routeId") String routeId) {
        try {
            camelContext.stopRoute(routeId);
            camelContext.removeRoute(routeId);

            return new ResponseEntity<>("Successfully stopped and removed route with ID "
                    + routeId, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Could not remove route with ID {} from Camel context. [exception=({})]",
                    routeId, e.getMessage());
            return new ResponseEntity<>("Could not stop or remove route: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
