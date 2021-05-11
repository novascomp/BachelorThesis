package me.novascomp.messages.config;

import java.util.Base64;
import java.util.Date;
import me.novascomp.messages.model.General;
import me.novascomp.utils.standalone.version.iNAppInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NVFUtils {

    public final static String AUTHORIZATION_HEADER = "Authorization";
    private final iNAppInformation nvfVersion;

    @Autowired
    public NVFUtils(iNAppInformation nvfVersion) {
        this.nvfVersion = nvfVersion;
    }

    public General getGeneral(String id) {
        General general = new General(id);
        general.setSwBuild(nvfVersion.getVersion().getApplicationSignature());
        general.setDate(new Date());
        general.setTime(new Date());
        return general;
    }

    public String getBase64(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public static String getBaseUrl(String currentUrl) {
        return currentUrl.split("NVM/")[0];
    }

}
