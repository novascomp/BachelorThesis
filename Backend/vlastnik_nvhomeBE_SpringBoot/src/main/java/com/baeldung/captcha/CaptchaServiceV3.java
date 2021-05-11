package com.baeldung.captcha;

import java.net.URI;
import java.util.logging.Level;
import me.novascomp.home.config.BeansInit;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service("captchaServiceV3")
public class CaptchaServiceV3 extends AbstractCaptchaService {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(CaptchaServiceV3.class.getName());

    @Override
    public void processResponse(String response, final String action, float requiredTreshold) throws ReCaptchaInvalidException {
        securityCheck(response);

        final URI verifyUri = URI.create(String.format(RECAPTCHA_URL_TEMPLATE, getReCaptchaSecret(), response, getClientIP()));

        try {
            final GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);
            LOG.log(Level.INFO, "Google''s response: {0}", googleResponse.toString());
            LOG.log(Level.INFO, "Required action: {0}", action);
            LOG.log(Level.INFO, "Required treshold: {0}", requiredTreshold);
            LOG.log(Level.INFO, BeansInit.DOT_SPACE);

            if (!googleResponse.isSuccess() || !googleResponse.getAction().equals(action) || googleResponse.getScore() < requiredTreshold) {
                if (googleResponse.hasClientError()) {
                    reCaptchaAttemptService.reCaptchaFailed(getClientIP());
                }
                throw new ReCaptchaInvalidException("reCaptcha was not successfully validated: score: " + googleResponse.getScore());
            }
        } catch (RestClientException rce) {
            throw new ReCaptchaUnavailableException("Registration unavailable at this time.  Please try again later.", rce);
        }
        reCaptchaAttemptService.reCaptchaSucceeded(getClientIP());
    }
}
