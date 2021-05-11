package com.baeldung.captcha;

public interface ICaptchaService {

    default void processResponse(final String response) throws ReCaptchaInvalidException {
    }

    default void processResponse(final String response, String action) throws ReCaptchaInvalidException {
    }

    default void processResponse(final String response, String action, float requiredTreshold) throws ReCaptchaInvalidException {
    }

    String getReCaptchaSite();

    String getReCaptchaSecret();
}
