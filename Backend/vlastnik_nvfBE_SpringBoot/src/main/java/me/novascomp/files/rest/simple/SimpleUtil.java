package me.novascomp.files.rest.simple;

import java.util.Optional;
import me.novascomp.files.rest.GeneralController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SimpleUtil<Model, Controller extends GeneralController> {

    private final Controller controller;

    public SimpleUtil(Controller controller) {
        this.controller = controller;
    }

    public Model getModelFromControllerResponse(ResponseEntity<?> responseEntity) {
        Model modelRecord = null;
        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            String[] pathArray = responseEntity.getHeaders().getLocation().toString().split("/");
            String id = pathArray[pathArray.length - 1];
            final Optional<Model> optionalFileRecord = controller.getService().findById(id);
            if (optionalFileRecord.isPresent()) {
                modelRecord = optionalFileRecord.get();
            }
        }
        return modelRecord;
    }

    public Controller getController() {
        return controller;
    }

}
