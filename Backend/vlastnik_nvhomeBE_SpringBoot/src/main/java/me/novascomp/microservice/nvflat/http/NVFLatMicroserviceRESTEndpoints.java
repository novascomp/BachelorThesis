package me.novascomp.microservice.nvflat.http;

import com.sun.istack.NotNull;
import me.novascomp.utils.microservice.communication.Endpoint;

public class NVFLatMicroserviceRESTEndpoints extends Endpoint {

    private final String flats = "flats/";
    private final String details = "details/";
    private final String organizations = "organizations/";
    private final String tokens = "tokens/";
    private final String residents = "residents/";
    private final String scopes = "scopes/";
    private final String byidentifier = "byidentifier/";
    private final String flatOrganizationsPersonal = "flats/personal/organizations";

    public NVFLatMicroserviceRESTEndpoints(String basePath) {
        super(basePath);
    }

    //FLAT
    public String getFlatByIdTokensEndpoint(@NotNull String flatId) {
        return basePath + flats + flatId + "/" + tokens;
    }

    public String getFlatByIdResidentsEndpoint(@NotNull String flatId) {
        return basePath + flats + flatId + "/" + residents;
    }

    public String getFlatByIdEndpoint(@NotNull String flatId) {
        return basePath + flats + flatId;
    }

    public String getFlatsEndpoint() {
        return basePath + flats;
    }

    //TOKENS
    public String getTokenByIdScopesEndpoint(@NotNull String tokenId) {
        return basePath + tokens + tokenId + "/" + scopes;
    }

    public String getTokenByIdEndpoint(@NotNull String tokenId) {
        return basePath + tokens + tokenId;
    }

    public String getTokensEndpoint() {
        return basePath + tokens;
    }

    public String getTokensUserAddByTokenKeyEndpoint() {
        return basePath + tokens + "user/add";
    }

    //DETAIL
    public String getDetailsEndpoint() {
        return basePath + details;
    }

    //ORGANIZATION
    public String getOrganizationFlatsByOrganizationIdEndpoint(@NotNull String organizationId) {
        return basePath + organizations + organizationId + "/" + flats;
    }

    public String getOrganizationsByIdEndpoint(@NotNull String organizationId) {
        return basePath + organizations + organizationId;
    }

    public String getOrganizationsEndpoint() {
        return basePath + organizations;
    }

    public String getOrganizationsFlatByIdentifierEndpoint(@NotNull String organizationId, @NotNull String flatIdentifier) {
        return basePath + organizations + organizationId + "/flats/" + byidentifier + flatIdentifier;
    }

    public String getOrganizationsFlatsDefaultTokensDocumentEndpoint(@NotNull String organizationId, @NotNull String flatId) {
        return basePath + organizations + organizationId + "/flats/" + flatId + "/defaultdocument";
    }

    public String getFlatOrganizationsPersonal() {
        return flatOrganizationsPersonal;
    }

    //SCOPE
    public String getScopesEndpoint() {
        return basePath + scopes;
    }
}
