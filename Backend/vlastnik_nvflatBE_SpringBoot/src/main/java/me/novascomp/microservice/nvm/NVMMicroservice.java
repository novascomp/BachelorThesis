package me.novascomp.microservice.nvm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.microservice.nvm.model.LightweightComponent;
import me.novascomp.microservice.nvm.model.LightweightMessage;
import me.novascomp.utils.microservice.communication.Microservice;
import me.novascomp.utils.microservice.oauth.MicroserviceCredentials;

@Service
public class NVMMicroservice extends Microservice {

    private final NVMMicroserviceRESTEndpoints endpoints;

    public NVMMicroservice(MicroserviceCredentials microserviceCredentials, ObjectMapper objectMapper, String nvmPath) {
        super(microserviceCredentials, objectMapper);
        endpoints = new NVMMicroserviceRESTEndpoints(nvmPath);
    }

    public HttpResponse<String> getMessageById(@NotNull String nvmId) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequest(endpoints.getMessageByIdPath(nvmId));
        return returnHttpResponse(request);
    }

    public HttpResponse<String> createMessage(@NotNull String organizationId, @NotNull String heading, @NotNull String body) throws IOException, InterruptedException, ConnectException {
        LightweightMessage lightweightMessage = new LightweightMessage(heading, body, organizationId);
        HttpRequest request = postWhateverByLinkRequest(endpoints.getMessagesPath(), lightweightMessage);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> deleteMessageById(@NotNull String nvmId) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = deleteWhateverByLinkRequest(endpoints.getMessageByIdPath(nvmId));
        return returnHttpResponse(request);
    }

    public HttpResponse<String> uploadFile(@NotNull String messageId, @NotNull File file) throws URISyntaxException, IOException, InterruptedException {
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put("file", Path.of(file.getPath()));
        HttpResponse<String> stringHttpResponse = sendMultipartFormDataByPath(map, endpoints.getUploadNvfFileById(messageId), file.getName());
        return stringHttpResponse;
    }

    public HttpResponse<String> uploadFile(@NotNull String messageId, @NotNull MultipartFile file) throws URISyntaxException, IOException, InterruptedException {
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put("file", file);
        HttpResponse<String> stringHttpResponse = sendMultipartFormData(map, endpoints.getUploadNvfFileById(messageId));
        return stringHttpResponse;
    }

    public HttpResponse<String> getMessagesByCategoryHierarchy(@NotNull Object component, Pageable pageable) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = postWhateverByLinkRequest(endpoints.getMessagesByCategoryHierarchy(), component, pageable);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> getMessagesByCategories(@NotNull Object component, Pageable pageable) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = postWhateverByLinkRequest(endpoints.getMessagesByCategoriesPath(), component, pageable);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> postComponentToMessage(@NotNull String componentName, @NotNull String messageId, @NotNull Object component) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = postWhateverByLinkRequest(endpoints.getMessageComponentPathById(componentName, messageId), component);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> getFileShares(@NotNull String nvfFileId, Pageable pageable) throws IOException, InterruptedException, ConnectException {
        return getWhateverByLinkResponse(endpoints.getFileByIdSharesPath(nvfFileId), pageable);
    }

    public HttpResponse<String> getFileComponentById(@NotNull String componentName, @NotNull String messageId, @NotNull String componentId) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequest(endpoints.getMessageComponentPathById(componentName, messageId, componentId));
        return returnHttpResponse(request);
    }

    public HttpResponse<String> deleteFileComponentById(@NotNull String componentName, @NotNull String messageId, @NotNull String componentId) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = deleteWhateverByLinkRequest(endpoints.getMessageComponentPathById(componentName, messageId, componentId));
        return returnHttpResponse(request);
    }

    public HttpResponse<String> getComponentById(@NotNull String componentName, @NotNull String componentId) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequest(endpoints.getComponentPathById(componentName, componentId));
        return returnHttpResponse(request);
    }

    public HttpResponse<String> getComponentsByCreatorKeyAndText(@NotNull String componentName, @NotNull Object component) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = postWhateverByLinkRequest(endpoints.getComponentByCreatorAndText(componentName), component);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> getComponentsByCreatorKey(@NotNull String creatorKey, @NotNull String componentName, Pageable pageable) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequest(endpoints.getComponentByCreatorPath(componentName, creatorKey), pageable);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> deleteMessageComponentList(@NotNull String creatorText, @NotNull String componentName, @NotNull List<String> textList) throws IOException, InterruptedException, ConnectException {
        List<LightweightComponent> components = new ArrayList<>();
        textList.forEach((text) -> {
            components.add(new LightweightComponent(text, creatorText));
        });
        HttpRequest request = postWhateverByLinkRequest(endpoints.getComponentListDeletePath(componentName), components);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> createMessageComponentList(@NotNull String creatorText, @NotNull String componentName, @NotNull List<String> textList) throws IOException, InterruptedException, ConnectException {
        List<LightweightComponent> components = new ArrayList<>();
        textList.forEach((text) -> {
            components.add(new LightweightComponent(text, creatorText));
        });
        HttpRequest request = postWhateverByLinkRequest(endpoints.getComponentListPath(componentName), components);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> createMessageComponent(@NotNull String creatorText, @NotNull String componentName, @NotNull String text) throws IOException, InterruptedException, ConnectException {
        LightweightComponent component = new LightweightComponent(text, creatorText);
        HttpRequest request = postWhateverByLinkRequest(endpoints.getComponentPath(componentName), component);
        return returnHttpResponse(request);
    }

    public HttpResponse<String> deleteComponentFromMessage(@NotNull String messageId, @NotNull String componentId) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = deleteWhateverByLinkRequest(endpoints.getMessagesDeleteCategoriesPath(messageId, componentId));
        return returnHttpResponse(request);
    }

    public HttpResponse<String> deleteMessageComponent(@NotNull String organizationId, @NotNull String componentName, @NotNull String componentId) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = deleteWhateverByLinkRequest(endpoints.getComponentPathById(componentName, componentId));
        return returnHttpResponse(request);
    }

    public HttpResponse<String> getMessageByCreatorKey(@NotNull String creatorKey, Pageable pageable) throws IOException, InterruptedException, ConnectException {
        HttpRequest request = getWhateverByLinkRequest(endpoints.getMessagesByCreatorKeyPath(creatorKey), pageable);
        return returnHttpResponse(request);
    }
}
