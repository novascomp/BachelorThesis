package me.novascomp.messages.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import me.novascomp.utils.microservice.oauth.MicroserviceCredentials;
import me.novascomp.utils.standalone.version.NAppInformationImpl;
import me.novascomp.utils.standalone.version.NVersionImpl;
import me.novascomp.utils.standalone.version.iNAppInformation;
import me.novascomp.utils.standalone.version.iNVersion;

@Configuration
@ComponentScan(basePackages = "me.novascomp.utils")
@ComponentScan(basePackages = "me.novascomp.microservice")
@PropertySource(name = "appInformation", value = "app_information.properties")
public class BeansInit {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${creator}")
    private String creator;

    @Value("${company}")
    private String company;

    @Value("${email}")
    private String email;

    @Value("${webPage}")
    private String webpage;

    @Value("${dateOfRelease}")
    private String dateOfRelease;

    @Value("${productName}")
    private String productName;

    @Value("${productVersion}")
    private String productVersion;

    @Value("${productBuild}")
    private String productBuild;

    ///
    /// General Microservice
    ///
    @Value("${microservice.token.service}")
    private String microserviceTokenService;

    @Value("${microservice.clientId}")
    private String microserviceClientId;

    @Value("${microservice.secretId}")
    private String microserviceSecretId;

    ///
    /// NVHOME
    ///
    @Value("${nvhome.root}")
    private String nvhomeRoot;

    @Value("${nvhome.scope}")
    private String nvhomeScope;

    @Value("${nvhome.hosting}")
    private String nvhomeHosting;

    ///
    /// NVFLAT
    ///
    @Value("${nvflat.root}")
    private String nvflatRoot;

    @Value("${nvflat.scope}")
    private String nvflatScope;

    @Value("${nvflat.hosting}")
    private String nvflatHosting;

    ///
    /// NVM
    ///
    @Value("${nvm.root}")
    private String nvmRoot;

    @Value("${nvm.scope}")
    private String nvmScope;

    @Value("${nvm.hosting}")
    private String nvmHosting;

    ///
    /// NVF
    ///
    @Value("${nvf.root}")
    private String nvfRoot;

    @Value("${nvf.scope}")
    private String nvfScope;

    @Value("${nvf.hosting}")
    private String nvfHosting;

    @Bean(name = "applicationMainScope")
    public String getApplicationMainScope() {
        return nvmScope;
    }

    @Bean(name = "production")
    public boolean getProductionStatus() {
        return "prod".equals(profile);
    }

    @Bean(name = "nvhomeScope")
    public String getNvhomeScope() {
        return nvhomeScope;
    }

    @Bean(name = "nvflatScope")
    public String getNvflatScope() {
        return nvflatScope;
    }

    @Bean(name = "nvmScope")
    public String getNvmScope() {
        return nvmScope;
    }

    @Bean(name = "nvfScope")
    public String getNvfScope() {
        return nvfScope;
    }

    @Bean(name = "nvhomePath")
    public String getNvhomePath() {
        return nvhomeHosting + nvhomeRoot;
    }

    @Bean(name = "nvflatPath")
    public String getNvflatPath() {
        return nvflatHosting + nvflatRoot;
    }

    @Bean(name = "nvmPath")
    public String getNvmPath() {
        return nvmHosting + nvmRoot;
    }

    @Bean(name = "nvfPath")
    public String getNvfPath() {
        return nvfHosting + nvfRoot;
    }

    private String getAllMicroserviceScopes() {
        return nvhomeScope + " " + nvflatScope + " " + nvmScope + " " + nvfScope;
    }

    public static final String DOT_SPACE = "-------------------------------";

    @Bean(name = "nvfVersion")
    public iNAppInformation nvfVersion() {
        iNVersion version = new NVersionImpl(dateOfRelease, productName, productVersion, productBuild);
        return new NAppInformationImpl(nvhomeRoot, creator, company, email, webpage, version);
    }

    @Bean
    public MicroserviceCredentials getMicroserviceCredentials() {
        return new MicroserviceCredentials(microserviceTokenService, microserviceClientId, microserviceSecretId, getAllMicroserviceScopes());
    }

}
