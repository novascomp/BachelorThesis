package me.novascomp.microservice.nvm.model;

import java.util.Date;

public class CreateDocumentSecurity {

    private final Date createRequestDate;
    private final String documentId;

    public CreateDocumentSecurity(Date createRequestDate, String documentId) {
        this.createRequestDate = createRequestDate;
        this.documentId = documentId;
    }

    public Date getCreateRequestDate() {
        return createRequestDate;
    }

    public String getDocumentId() {
        return documentId;
    }
}
