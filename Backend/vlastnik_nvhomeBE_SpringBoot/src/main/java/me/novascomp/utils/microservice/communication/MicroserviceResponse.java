package me.novascomp.utils.microservice.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.data.domain.Page;

public class MicroserviceResponse<T> {

    protected final ObjectMapper objectMapper;

    public MicroserviceResponse(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Page<T> parsePageResponse(HttpResponse<String> response) {
        try {
            return objectMapper.readValue(response.body(), new TypeReference<RestResponsePage<T>>() {
            });
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MicroserviceResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public T parseSingleResponse(HttpResponse<String> response, Class<T> typeParameterClass) {
        try {
            return objectMapper.readValue(response.body(), typeParameterClass);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MicroserviceResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
