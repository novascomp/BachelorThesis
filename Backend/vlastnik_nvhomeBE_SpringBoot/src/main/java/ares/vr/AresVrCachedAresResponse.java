package ares.vr;

import java.util.Date;

public class AresVrCachedAresResponse {

    private final Date date;
    private final AresVrResponse aresResponse;

    public AresVrCachedAresResponse(Date date, AresVrResponse aresResponse) {
        this.date = date;
        this.aresResponse = aresResponse;
    }

    public Date getDate() {
        return date;
    }

    public AresVrResponse getAresResponse() {
        return aresResponse;
    }

}
