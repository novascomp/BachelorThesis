package me.novascomp.utils.microservice.communication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

//https://stackoverflow.com/questions/52490399/spring-boot-page-deserialization-pageimpl-no-constructor
public class RestResponsePage<T> extends PageImpl<T> {

    private static final long serialVersionUID = 3248189030448292002L;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestResponsePage(@JsonProperty("content") List<T> content, @JsonProperty("number") int number, @JsonProperty("size") int size,
            @JsonProperty("pageable") JsonNode pageable, @JsonProperty("totalPages") int totalPages, @JsonProperty("last") boolean last,
            @JsonProperty("totalElements") Long totalElements, @JsonProperty("sort") JsonNode sort, @JsonProperty("numberOfElements") int numberOfElements, @JsonProperty("first") boolean first
    ) {
        super(content, PageRequest.of(number, size), totalElements);
    }

    public RestResponsePage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestResponsePage(List<T> content) {
        super(content);
    }

    public RestResponsePage() {
        super(new ArrayList<T>());
    }

}
