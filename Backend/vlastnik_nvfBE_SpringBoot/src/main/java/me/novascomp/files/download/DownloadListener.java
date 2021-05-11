package me.novascomp.files.download;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;

@WebListener
public class DownloadListener implements AsyncListener {

    private final DownloadStatus downloadStatus;
    private static final Logger LOG = Logger.getLogger(DownloadListener.class.getName());

    public DownloadListener(DownloadStatus downloadStatus) {
        LOG.log(Level.INFO, "DownloadListener: Scope Session: started");
        this.downloadStatus = downloadStatus;
        this.downloadStatus.setDownloadInProgress(true);
    }

    @Override
    public void onComplete(AsyncEvent asyncEvent) throws IOException {
        LOG.log(Level.INFO, "DownloadListener: onComplete");
        downloadStatus.setDownloadInProgress(false);
    }

    @Override
    public void onError(AsyncEvent asyncEvent) throws IOException {
        LOG.log(Level.INFO, "DownloadListener: onError");
        downloadStatus.setDownloadInProgress(false);
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        System.out.println("AppAsyncListener onStartAsync");
    }

    @Override
    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        System.out.println("AppAsyncListener onTimeout");
        ServletResponse response = asyncEvent.getAsyncContext().getResponse();
        PrintWriter out = response.getWriter();
        out.write("TimeOut Error in Processing");
    }

}
