package me.novascomp.files.config;

import me.novascomp.files.model.General;
import me.novascomp.files.version.iNAppInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

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

}
