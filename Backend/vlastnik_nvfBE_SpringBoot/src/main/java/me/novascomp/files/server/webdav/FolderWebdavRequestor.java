package me.novascomp.files.server.webdav;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import me.novascomp.files.config.NVFUtils;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.Server;
import me.novascomp.utils.standalone.service.exceptions.MicroserviceConnectionException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class FolderWebdavRequestor extends WebdavRequestor {

    private Folder previousFolder;

    public FolderWebdavRequestor(Server server, NVFUtils nvfUtils) {
        super(nvfUtils, server);
    }

    public FolderWebdavRequestor(Folder previousFolder, Server server, NVFUtils nvfUtils) {
        super(nvfUtils, server);
        this.previousFolder = previousFolder;
    }

    public HttpStatus createFolder(String folderName) {

        HttpResponse<String> response = null;

        try {
            response = sendCommand(getFolderPath(previousFolder) + folderName, BodyPublishers.noBody(), WebDavCommand.CREATE_FOLDER);
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        }

        return getStatusCode(response);
    }

    public HttpStatus deleteFolder(String folderName) {

        HttpResponse<String> response = null;

        try {
            response = sendCommand(getFolderPath(previousFolder) + folderName, BodyPublishers.noBody(), WebDavCommand.DELETE_FOLDER);
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        }

        return getStatusCode(response);
    }
}
