package me.novascomp.utils.rest;

import java.net.URI;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.baeldung.captcha.CaptchaServiceV3;
import com.baeldung.captcha.ReCaptchaInvalidException;
import com.baeldung.captcha.ReCaptchaUnavailableException;
import com.sun.istack.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import me.novascomp.home.config.BeansInit;
import me.novascomp.home.model.General;
import me.novascomp.utils.standalone.version.iNAppInformation;
import me.novascomp.utils.standalone.service.exceptions.SecurityException;

@Service
public class RestUtils {

    private static final Logger LOG = Logger.getLogger(RestUtils.class.getName());

    public final static String RECAPTCHA_VERIFY_ICO_ACTION = "registrationVerifyIco";
    public final static String RECAPTCHA_REGISTER_ORGANIZATION = "registrationRegisterOrganization";
    public final static String RECAPTCHA_PIN_TOKEN = "pinToken";
    public final static String RECAPTCHA_TOKEN = "token";
    public final static String RECAPTCHA_PERSON = "person";
    public final static String RECAPTCHA_COMMITTEE = "committee";
    public final static String RECAPTCHA_COMPONENT = "component";
    public final static String RECAPTCHA_FLATS_UPLOAD = "flatsUpload";
    public final static String RECAPTCHA_FLATS_DELETE = "flatsDelete";
    public final static String RECAPTCHA_DOCUMENT = "document";
    public final static String RECAPTCHA_ARES = "ares";

    public final static float RECAPTCHA_SCORE_0_9 = 0.7F;
    public final static float RECAPTCHA_SCORE_0_8 = 0.7F;
    public final static float RECAPTCHA_SCORE_0_7 = 0.7F;
    public final static float RECAPTCHA_SCORE_0_5 = 0.7F;

    public final static String AUTHORIZATION_HEADER = "Authorization";
    private static iNAppInformation version;

    private static CaptchaServiceV3 captchaServiceV3;

    @Autowired
    public RestUtils(iNAppInformation nvfVersion, CaptchaServiceV3 captchaServiceV3) {
        RestUtils.version = nvfVersion;
        RestUtils.captchaServiceV3 = captchaServiceV3;
    }

    public General getGeneral(String id) {
        General general = new General(id);
        general.setSwBuild(version.getVersion().getApplicationSignature());
        general.setDate(new Date());
        general.setTime(new Date());
        return general;
    }

    public String getBase64(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public static String getBaseUrl(String currentUrl) {
        return currentUrl.split(version.getContextRoot())[0];
    }

    public static String getRoot() {
        return version.getContextRoot();
    }

    public static HttpHeaders createLocationHeaderFromCurrentUri(String path, Object... uriVariableValues) {
        assert path != null;

        final URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path(path).buildAndExpand(
                uriVariableValues).toUri();
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.LOCATION, location.toASCIIString());
        return headers;
    }

    public static void verifyRecaptcha(String applicationMainScope, List<String> scopes, @NotNull String recaptchaToken, @NotNull String action, @NotNull float requiredTreshold) throws SecurityException {
        if (!scopes.contains(applicationMainScope)) {
            try {
                captchaServiceV3.processResponse(recaptchaToken, action, requiredTreshold);
            } catch (ReCaptchaInvalidException | ReCaptchaUnavailableException captchaInvalidException) {
                LOG.log(Level.INFO, captchaInvalidException.toString());
                LOG.log(Level.INFO, BeansInit.DOT_SPACE);
                throw new SecurityException();
            }
        }
    }
}
