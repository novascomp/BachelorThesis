package me.novascomp.microservice.nvflat.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import me.novascomp.home.flat.uploader.FlatUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import me.novascomp.home.model.Organization;
import me.novascomp.microservice.nvflat.model.LightweightDetail;
import me.novascomp.microservice.nvflat.model.LightweightFlat;
import me.novascomp.microservice.nvflat.model.LightweightScope;
import me.novascomp.microservice.nvflat.model.LightweightToken;
import me.novascomp.utils.microservice.communication.Microservice;
import me.novascomp.utils.microservice.oauth.MicroserviceCredentials;
import org.springframework.web.bind.annotation.RequestBody;

public class NVFLatMicroservice extends Microservice {

    private final NVFLatMicroserviceRESTEndpoints endpoints;

    @Autowired
    public NVFLatMicroservice(MicroserviceCredentials microserviceCredentials, ObjectMapper objectMapper, String nvflatPath) {
        super(microserviceCredentials, objectMapper);
        endpoints = new NVFLatMicroserviceRESTEndpoints(nvflatPath);
    }

    public HttpResponse<String> uploadFile(@NotNull String organizationId, @NotNull String flatId, @NotNull File file) throws URISyntaxException, IOException, InterruptedException {
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put("file", Path.of(file.getPath()));
        HttpResponse<String> stringHttpResponse = sendMultipartFormDataByPath(map, endpoints.getOrganizationsFlatsDefaultTokensDocumentEndpoint(organizationId, flatId), file.getName());
        return stringHttpResponse;
    }

    //ORGANIZATION
    protected HttpResponse<String> postOrganization(@NotNull String organizationId, @NotNull String ico) throws IOException, InterruptedException {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization.setIco(ico);
        return postWhateverByLinkResponse(endpoints.getOrganizationsEndpoint(), organization);
    }

    protected HttpResponse<String> uploadOrganizationFlats(@NotNull String organizationId, @RequestBody @NotNull FlatUploader flats) throws IOException, InterruptedException {
        return postWhateverByLinkResponse(endpoints.getOrganizationFlatsByOrganizationIdEndpoint(organizationId), flats);
    }

    protected HttpResponse<String> deleteOrganizationById(@NotNull String organizationId) throws IOException, InterruptedException {
        return deleteWhateverByLinkResponse(endpoints.getOrganizationsByIdEndpoint(organizationId));
    }

    protected HttpResponse<String> deleteOrganizationFlat(@NotNull String flatId) throws IOException, InterruptedException {
        return deleteWhateverByLinkResponse(endpoints.getFlatByIdEndpoint(flatId));
    }

    protected HttpResponse<String> deleteAllOrganizationFlats(@NotNull String organizationId) throws IOException, InterruptedException {
        return deleteWhateverByLinkResponse(endpoints.getOrganizationFlatsByOrganizationIdEndpoint(organizationId));
    }

    protected HttpResponse<String> getOrganizationFlats(@NotNull String organizationId, @NotNull Pageable pageable) throws IOException, InterruptedException {
        return getWhateverByLinkResponse(endpoints.getOrganizationFlatsByOrganizationIdEndpoint(organizationId), pageable);
    }

    protected HttpResponse<String> getOrganizationsFlatByIdentifierEndpoint(@NotNull String organizationId, @NotNull String flatIdentifier) throws IOException, InterruptedException {
        return getWhateverByLinkResponse(endpoints.getOrganizationsFlatByIdentifierEndpoint(organizationId, flatIdentifier));
    }

    //FLAT
    protected Optional<LightweightFlat> getFlatById(@NotNull String nvflatFlatId) throws IOException, InterruptedException {
        HttpResponse<String> response = getWhateverByLinkResponse(endpoints.getFlatByIdEndpoint(nvflatFlatId));

        LightweightFlat flat = null;
        if (HttpStatus.valueOf(response.statusCode()).is2xxSuccessful()) {
            flat = objectMapper.readValue(response.body(), LightweightFlat.class);
        }

        return Optional.ofNullable(flat);
    }

    protected HttpResponse<String> postFlat(@NotNull String flatIdentifier, @NotNull String nvflatOrganizationId) throws IOException, InterruptedException {
        Organization organization = new Organization();
        organization.setOrganizationId(nvflatOrganizationId);

        LightweightFlat lightweightFlat = new LightweightFlat();
        lightweightFlat.setIdentifier(flatIdentifier);
        lightweightFlat.setOrganization(organization);

        return postWhateverByLinkResponse(endpoints.getFlatsEndpoint(), lightweightFlat);
    }

    protected HttpResponse<String> getFlatTokens(@NotNull String flatId, @NotNull Pageable pageable) throws IOException, InterruptedException {
        return getWhateverByLinkResponse(endpoints.getFlatByIdTokensEndpoint(flatId), pageable);
    }

    protected HttpResponse<String> getFlatResidents(@NotNull String flatId, @NotNull Pageable pageable) throws IOException, InterruptedException {
        return getWhateverByLinkResponse(endpoints.getFlatByIdResidentsEndpoint(flatId), pageable);
    }

    protected HttpResponse<String> getFlatToken(@NotNull String tokenId) throws IOException, InterruptedException {
        return getWhateverByLinkResponse(endpoints.getTokenByIdEndpoint(tokenId));
    }

    protected HttpResponse<String> getFlatTokenScopes(@NotNull String tokenId, @NotNull Pageable pageable) throws IOException, InterruptedException {
        return getWhateverByLinkResponse(endpoints.getTokenByIdScopesEndpoint(tokenId), pageable);
    }

    protected HttpResponse<String> postFlatTokens(@NotNull String flatId) throws IOException, InterruptedException {
        LightweightToken token = new LightweightToken();
        LightweightFlat flat = new LightweightFlat();
        flat.setFlatId(flatId);
        token.setFlat(flat);
        return postWhateverByLinkResponse(endpoints.getTokensEndpoint(), token);
    }

    protected HttpResponse<String> postFlatTokensKeyDefined(@NotNull String flatId, @NotNull String tokenKey) throws IOException, InterruptedException {
        LightweightToken token = new LightweightToken();
        token.setKey(tokenKey);
        LightweightFlat flat = new LightweightFlat();
        flat.setFlatId(flatId);
        token.setFlat(flat);
        return postWhateverByLinkResponse(endpoints.getTokensEndpoint(), token);
    }

    protected HttpResponse<String> deleteFlatToken(@NotNull String tokenId) throws IOException, InterruptedException {
        return deleteWhateverByLinkResponse(endpoints.getTokenByIdEndpoint(tokenId));
    }

    protected HttpResponse<String> postDetail(@NotNull String size, @NotNull String commonShareSize, @NotNull String nvflatFlatId) throws IOException, InterruptedException {
        LightweightFlat lightweightFlat = new LightweightFlat();
        lightweightFlat.setFlatId(nvflatFlatId);

        LightweightDetail lightweightDetail = new LightweightDetail();
        lightweightDetail.setSize(size);
        lightweightDetail.setCommonShareSize(commonShareSize);
        lightweightDetail.setFlat(lightweightFlat);

        return postWhateverByLinkResponse(endpoints.getDetailsEndpoint(), lightweightDetail);
    }

    protected HttpResponse<String> postUserToTokenByTokenKey(@NotNull String tokenKey, @NotNull String principalToken) throws IOException, InterruptedException {
        LightweightToken token = new LightweightToken();
        token.setKey(tokenKey);
        return postWhateverByLinkResponse(endpoints.getTokensUserAddByTokenKeyEndpoint(), token, principalToken);
    }

    //SCOPES
    protected HttpResponse<String> postScopeToToken(@NotNull String tokenId, @NotNull String scopeId) throws IOException, InterruptedException {
        LightweightScope scope = new LightweightScope();
        scope.setScopeId(scopeId);
        return postWhateverByLinkResponse(endpoints.getTokenByIdScopesEndpoint(tokenId), scope);
    }

    protected HttpResponse<String> getAllScopesResponse() throws IOException, InterruptedException {
        return getWhateverByLinkUnlimitedPageableResponse(endpoints.getScopesEndpoint());
    }

    protected HttpRequest getOrganizationsContainingUserFlatsRequest(String bearerToken) {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.AUTHORIZATION, BEARER + bearerToken)
                .GET()
                .uri(URI.create(endpoints.getBasePath() + endpoints.getFlatOrganizationsPersonal() + addQueryParamUnlimited()))
                .timeout(Duration.of(5, SECONDS))
                .build();
    }
}
