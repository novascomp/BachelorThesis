package me.novascomp.files.download;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadStatus {

    private boolean downloadInProgress;
    private static final Logger LOG = Logger.getLogger(DownloadStatus.class.getName());

    public boolean isDownloadInProgress() {
        return downloadInProgress;
    }

    public void setDownloadInProgress(boolean downloadInProgress) {
        LOG.log(Level.INFO, "Download Status Progress: {0}", downloadInProgress);
        this.downloadInProgress = downloadInProgress;
    }

}
