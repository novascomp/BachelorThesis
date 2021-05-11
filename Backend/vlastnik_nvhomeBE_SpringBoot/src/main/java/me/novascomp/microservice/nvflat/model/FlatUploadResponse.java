package me.novascomp.microservice.nvflat.model;

import me.novascomp.home.flat.uploader.NVHomeFlat;
import java.util.concurrent.Future;

public class FlatUploadResponse {

    private final NVHomeFlat flatUpload;
    private final Future<String> flatFuture;

    public FlatUploadResponse(NVHomeFlat flatUpload, Future<String> flatFuture) {
        this.flatUpload = flatUpload;
        this.flatFuture = flatFuture;
    }

    public NVHomeFlat getFlatUpload() {
        return flatUpload;
    }

    public Future<String> getFlatFuture() {
        return flatFuture;
    }

}
