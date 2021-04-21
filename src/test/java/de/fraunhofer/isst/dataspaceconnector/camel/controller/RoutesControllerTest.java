package de.fraunhofer.isst.dataspaceconnector.camel.controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {RoutesController.class})
public class RoutesControllerTest {

    @MockBean
    private DefaultCamelContext camelContext;

    @MockBean
    private Unmarshaller unmarshaller;

    @Autowired
    @InjectMocks
    private RoutesController routesController;

    @Test
    public void addRoutes_fileNull_returnStatusCode400() {
        /* ACT */
        final var response = routesController.addRoutes(null);

        /* ASSERT */
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void addRoutes_validRouteFile_returnStatusCode200() throws Exception {
        /* ARRANGE */
        when(unmarshaller.unmarshal(any(InputStream.class))).thenReturn(new RoutesDefinition());
        doNothing().when(camelContext).addRouteDefinitions(any());

        final var file = new MockMultipartFile("file", "routes.xml",
                "application/xml",
                getRouteFileContent().getBytes(StandardCharsets.UTF_8));

        /* ACT */
        final var response = routesController.addRoutes(file);

        /* ASSERT */
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void addRoutes_invalidRouteFile_returnStatusCode400() throws Exception {
        /* ARRANGE */
        when(unmarshaller.unmarshal(any(InputStream.class))).thenThrow(JAXBException.class);

        final var file = new MockMultipartFile("file", "routes.xml",
                "application/xml",
                getRouteFileContent().getBytes(StandardCharsets.UTF_8));

        /* ACT */
        final var response = routesController.addRoutes(file);

        /* ASSERT */
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void addRoutes_errorAddingRoutesToContext_returnStatusCode500() throws Exception {
        /* ARRANGE */
        when(unmarshaller.unmarshal(any(InputStream.class))).thenReturn(new RoutesDefinition());
        doThrow(Exception.class).when(camelContext).addRouteDefinitions(any());

        final var file = new MockMultipartFile("file", "routes.xml",
                "application/xml",
                getRouteFileContent().getBytes(StandardCharsets.UTF_8));

        /* ACT */
        final var response = routesController.addRoutes(file);

        /* ASSERT */
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void removeRoute_validRouteId_returnStatusCode200() throws Exception {
        /* ARRANGE */
        doNothing().when(camelContext).stopRoute(any());
        when(camelContext.removeRoute(anyString())).thenReturn(true);

        /* ACT */
        final var response = routesController.removeRoute("validId");

        /* ASSERT */
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void removeRoute_invalidRouteId_returnStatusCode500() throws Exception {
        /* ARRANGE */
        doThrow(Exception.class).when(camelContext).stopRoute(any());

        /* ACT */
        final var response = routesController.removeRoute("invalidId");

        /* ASSERT */
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void removeRoute_routeCannotBeStopped_returnStatusCode500() throws Exception {
        /* ARRANGE */
        doNothing().when(camelContext).stopRoute(any());
        when(camelContext.removeRoute(anyString())).thenReturn(false);

        /* ACT */
        final var response = routesController.removeRoute("validId");

        /* ASSERT */
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private String getRouteFileContent() {
        return "<routes xmlns=\"http://camel.apache.org/schema/spring\">\n"
        + "    <route id=\"route-id\">\n"
        + "        <from uri=\"timer:tick?period=3000\"/>\n"
        + "        <to uri=\"log:info\"/>\n"
        + "    </route>\n"
        + "</routes>";
    }

}
