package me.novascomp.files.download;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.files.model.FileShare;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
//@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DownloadSessionService {

    private final Map<String, FileShare> fileShares;
    private static final Logger LOG = Logger.getLogger(DownloadSessionService.class.getName());

    private final DownloadStatus downloadStatus;

    public DownloadSessionService() {
        fileShares = new HashMap<>();
        downloadStatus = new DownloadStatus();
        LOG.log(Level.INFO, "DownloadSessionService - scope session - created");
    }

    public FileShare getFileShare(String token) {
        return fileShares.get(token);
    }

    public String addFileShare(FileShare fileShare) {
        String token = UUID.randomUUID().toString();
        fileShares.put(token, fileShare);
        return token;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

}
