package me.novascomp.files.download;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.servlet.AsyncContext;

public class NVDownloadManager implements Runnable {

    private final AsyncContext asyncContext;
    private final URL url;

    public NVDownloadManager(AsyncContext asyncContext, URL url) {
        this.asyncContext = asyncContext;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            long startTime = System.currentTimeMillis();
            HttpURLConnection connection = initializeUrlConnection();
            //asyncContext.getResponse().setCharacterEncoding("UTF-8");
            int nRead;
            byte[] data = new byte[400000000];
            while ((nRead = connection.getInputStream().read(data, 0, data.length)) != -1) {
                asyncContext.getResponse().getOutputStream().write(data, 0, nRead);
            }
            asyncContext.getResponse().getOutputStream().close();
            connection.disconnect();
            asyncContext.complete();
            long endTime = System.currentTimeMillis();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getFileType(String contentType) {

        String ending = "";
        boolean start = false;
        for (char c : contentType.toCharArray()) {

            if (";".equals(String.valueOf(c))) {
                return ending;
            }

            if (start == true) {
                ending += c;
            }
            if ("/".equals(String.valueOf(c))) {
                start = true;
            }
        }
        return null;
    }

    private HttpURLConnection initializeUrlConnection() {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            asyncContext.getResponse().setContentType(connection.getContentType());
            asyncContext.getResponse().setContentLength(connection.getContentLength());
            asyncContext.getResponse().setLocale(Locale.getDefault());
            connection.connect();
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
