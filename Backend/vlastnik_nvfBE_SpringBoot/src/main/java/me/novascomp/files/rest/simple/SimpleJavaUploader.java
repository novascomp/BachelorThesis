package me.novascomp.files.rest.simple;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleJavaUploader {

    private static String UPLOADER_LINK;

    public static HttpResponse<String> uploadFile(String UPLOADER_LINK, File file, String fileName, String folder) {

        HttpResponse<String> httpResponse = null;
        try {
            SimpleJavaUploader.UPLOADER_LINK = UPLOADER_LINK;
            Map<Object, Object> map = new LinkedHashMap<>();
            map.put("file", Path.of(file.getPath()));
            map.put("folder", folder);
            httpResponse = sendMultipartFormData(map, fileName);
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            Logger.getLogger(SimpleJavaUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return httpResponse;
    }

    public static HttpResponse<String> sendMultipartFormData(Map<Object, Object> map, String filename) throws URISyntaxException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String boundary = new BigInteger(256, new Random()).toString();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "multipart/form-data;boundary=" + boundary)
                .POST(ofMimeMultipartData(map, boundary, filename))
                .uri(URI.create(UPLOADER_LINK))
                .timeout(Duration.of(1000, SECONDS))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    //https://golb.hplar.ch/2019/01/java-11-http-client.html
    private static HttpRequest.BodyPublisher ofMimeMultipartData(Map<Object, Object> data,
            String boundary, String filename) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);

            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + filename
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
