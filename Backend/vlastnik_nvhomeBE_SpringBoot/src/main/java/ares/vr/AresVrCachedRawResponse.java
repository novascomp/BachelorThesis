package ares.vr;

import java.util.Date;

public class AresVrCachedRawResponse {

    private final Date responseDate;
    private final String rawResponse;

    public AresVrCachedRawResponse(Date responseDate, String rawResponse) {
        this.responseDate = responseDate;
        this.rawResponse = rawResponse;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public String getRawResponse() {
        return rawResponse;
    }
}
