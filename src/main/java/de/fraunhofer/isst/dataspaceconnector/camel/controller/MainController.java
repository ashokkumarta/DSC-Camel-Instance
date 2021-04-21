package de.fraunhofer.isst.dataspaceconnector.camel.controller;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that returns a response for the application's root path.
 */
@RestController
@RequestMapping
@NoArgsConstructor
public class MainController {

    /**
     * Can be called to check if the application is running.
     *
     * @return a response entity with code 200
     */
    @GetMapping
    public ResponseEntity<String> rootPath() {
        return new ResponseEntity<>("Camel instance is running!", HttpStatus.OK);
    }

}
