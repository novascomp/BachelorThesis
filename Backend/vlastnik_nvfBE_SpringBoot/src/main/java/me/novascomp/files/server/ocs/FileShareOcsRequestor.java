package me.novascomp.files.server.ocs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.novascomp.files.config.NVFUtils;
import me.novascomp.files.model.File;
import me.novascomp.files.model.Server;
import me.novascomp.files.server.ocs.model.create.OcsShareFileBasic;
import me.novascomp.utils.standalone.service.exceptions.MicroserviceConnectionException;

public class FileShareOcsRequestor extends OcsRequestor {

    public FileShareOcsRequestor(NVFUtils nvfUtils, ObjectMapper objectMapper, Server server) {
        super(nvfUtils, objectMapper, server);
    }

    public HttpResponse<String> createBasicPublicShare(File file) {

        OcsShareFileBasic ocsShareFileBasic = new OcsShareFileBasic(file.getFileId(), getFolderPath(file.getFolderId()) + file.getFileId(), OcsShareCommand.PUBLIC_LINK.getCommand());
        try {
            String json = objectMapper.writeValueAsString(ocsShareFileBasic);
            HttpResponse<String> httpResponse = sendCommand(HttpRequest.BodyPublishers.ofString(json), "", OcsCommand.SHARE_FILE);
            return httpResponse;
        } catch (JsonProcessingException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (URISyntaxException | InterruptedException | IOException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        }
    }

    public HttpResponse<String> deleteBasicPublicShare(String id) {

        try {
            HttpResponse<String> httpResponse = sendCommand(HttpRequest.BodyPublishers.noBody(), "/" + id, OcsCommand.DELETE_SHARE_FILE);
            return httpResponse;
        } catch (JsonProcessingException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        } catch (URISyntaxException | InterruptedException | IOException ex) {
            throw new MicroserviceConnectionException(ex.toString());
        }
    }

}
