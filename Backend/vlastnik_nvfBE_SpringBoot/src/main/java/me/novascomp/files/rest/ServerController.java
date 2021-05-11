package me.novascomp.files.rest;

import com.sun.istack.NotNull;
import me.novascomp.files.model.Server;
import me.novascomp.files.services.ServerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/servers")
public class ServerController extends GeneralController<Server, ServerService> {

    public ServerController() {
    }

    @PreAuthorize("hasAuthority('SCOPE_nvfScope')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postServer(@RequestBody @NotNull Server server) {
        if (service.verifyServerRequest(server).isPresent()) {
            return service.verifyServerRequest(server).get();
        }
        service.addServer(server);
        final HttpHeaders headers = RestUtils.createLocationHeaderFromCurrentUri("/{id}", server.getServerId());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

}
