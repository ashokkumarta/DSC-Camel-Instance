package de.fraunhofer.isst.dataspaceconnector.camel.controller;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
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
@RequestMapping("/api/routes")
public class RoutesController {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesController.class);

    /**
     * The Camel context.
     */
    private final DefaultCamelContext camelContext;

    /**
     * Unmarshaller for reading route definitions from XML.
     */
    private final Unmarshaller unmarshaller;

    /**
     * Constructor for the RoutesController.
     *
     * @param camelContext the CamelContext.
     * @param unmarshaller the Unmarshaller.
     */
    @Autowired
    public RoutesController(final CamelContext camelContext, final Unmarshaller unmarshaller) {
        this.camelContext = (DefaultCamelContext) camelContext;
        this.unmarshaller = unmarshaller;
    }

    /**
     * Adds one or more routes from an XML file to the Camel context.
     *
     * @param file the XML file.
     * @return a response entity with code 200 or 500, if an error occurs.
     */
    @PostMapping
    public ResponseEntity<String> addRoutes(@RequestParam("file") final MultipartFile file) {
        try {
            if (file == null) {
                throw new IllegalArgumentException("File must not be null");
            }

            final var inputStream = file.getInputStream();
            final var routes = (RoutesDefinition) unmarshaller.unmarshal(inputStream);
            camelContext.addRouteDefinitions(routes.getRoutes());

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Added {} routes to the Camel Context.", routes.getRoutes().size());
            }

            return new ResponseEntity<>("Successfully added " + routes.getRoutes().size()
                    + " routes to Camel Context.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read XML file because file was null.");
            }
            return new ResponseEntity<>("File must not be null.", HttpStatus.BAD_REQUEST);
        } catch (JAXBException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read route(s) from XML file. [exception=({})]",
                        e.getMessage(), e);
            }
            return new ResponseEntity<>("Could not read route(s) from XML file: "
                    + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not add route(s) to Camel Context. [exception=({})]",
                        e.getMessage(), e);
            }
            return new ResponseEntity<>("Could not add route(s) to Camel Context: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a route from the Camel context by its ID.
     *
     * @param routeId the route ID.
     * @return a response entity with code 200 or 500, if an error occurs.
     */
    @DeleteMapping("/{routeId}")
    public ResponseEntity<String> removeRoute(@PathVariable("routeId") final String routeId) {
        try {
            camelContext.stopRoute(routeId);

            if (!camelContext.removeRoute(routeId)) {
                throw new Exception("Could not remove route because route was not stopped.");
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Stopped route with ID {} and removed it from the Camel Context",
                        routeId);
            }

            return new ResponseEntity<>("Successfully stopped and removed route with ID "
                    + routeId, HttpStatus.OK);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not remove route with ID {} from Camel context. [exception=({})]",
                        routeId, e.getMessage(), e);
            }
            return new ResponseEntity<>("Could not stop or remove route: "
                    + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
