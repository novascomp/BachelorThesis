package me.novascomp.files.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.files.version.iNAppInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AppController {

    private final iNAppInformation appInformation;

    @Autowired
    public AppController(iNAppInformation appInformation) {
        this.appInformation = appInformation;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRedirectToFE() {
        final HttpHeaders headers = new HttpHeaders();
        try {
            headers.setLocation(new URI("https://nvf.novascomp.synology.me"));
        } catch (URISyntaxException ex) {
            Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @GetMapping(value = "/about", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getNAppInformationImpl() {
        return new ResponseEntity<>(appInformation, HttpStatus.OK);
    }
}
