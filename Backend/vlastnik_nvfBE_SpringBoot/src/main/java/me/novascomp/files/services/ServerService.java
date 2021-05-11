package me.novascomp.files.services;

import java.util.Optional;
import java.util.UUID;
import me.novascomp.files.model.General;
import me.novascomp.files.model.Server;
import me.novascomp.files.repository.ServerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ServerService extends GeneralService<Server, ServerRepository> {

    public ServerService() {
    }

    public void addServer(Server server) {
        String id = UUID.randomUUID().toString();
        server.setServerId(id);
        General general = nvfUtils.getGeneral(id);
        server.setGeneral(general);
        repository.save(server);
    }

    public Optional<Server> findByBaseUrl(String baseUrl) {
        return repository.findByBaseUrl(baseUrl);
    }

    public Optional<ResponseEntity> verifyServerRequest(Server server) {
        if (findByBaseUrl(server.getBaseUrl()).isPresent()) {
            return Optional.ofNullable(new ResponseEntity<>("base URL", HttpStatus.CONFLICT));
        } else {
            return Optional.ofNullable(null);
        }
    }

    public boolean existsByBaseUrl(String baseUrl) {
        return repository.existsByBaseUrl(baseUrl);
    }
}
