package me.novascomp.files.server.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import me.novascomp.files.config.NVFUtils;
import me.novascomp.files.model.File;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.Server;
import me.novascomp.utils.standalone.service.exceptions.MicroserviceConnectionException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Scope("prototype")
public class FileWebdavRequestor extends WebdavRequestor {

    public FileWebdavRequestor(NVFUtils nvfUtils, Server server) {
        super(nvfUtils, server);
    }

    public HttpStatus createFile(Folder folder, MultipartFile file, String fileId) {

        HttpResponse<String> response = null;

        try {
            try (InputStream inputStream = file.getInputStream()) {
                response = sendCommand(getFolderPath(folder) + fileId, BodyPublishers.ofByteArray(inputStream.readAllBytes()), WebDavCommand.CREATE_FILE);
            }
            System.gc();
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        }

        return getStatusCode(response);
    }

    public HttpStatus deleteFile(File file) {

        HttpResponse<String> response = null;

        try {
            response = sendCommand(getFolderPath(file.getFolderId()) + file.getFileId(), BodyPublishers.noBody(), WebDavCommand.DELETE_FILE);
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        }

        return getStatusCode(response);
    }

}
