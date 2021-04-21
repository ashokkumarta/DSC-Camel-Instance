package de.fraunhofer.isst.dataspaceconnector.camel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {MainController.class})
public class MainControllerTest {

    @Autowired
    private MainController mainController;

    @Test
    public void basePath() {
        /* ACT */
        final var response = mainController.rootPath();

        /* ASSERT */
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
