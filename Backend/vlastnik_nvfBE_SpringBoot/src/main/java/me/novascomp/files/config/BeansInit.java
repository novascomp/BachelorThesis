package me.novascomp.files.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import me.novascomp.files.download.AppContextListener;
import me.novascomp.files.download.WebDownloadAsyncServlet;
import me.novascomp.files.version.NAppInformationImpl;
import me.novascomp.files.version.NVersionImpl;
import me.novascomp.files.version.iNAppInformation;
import me.novascomp.files.version.iNVersion;

@Configuration
@Import({AppContextListener.class})
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

    public static final String DOT_SPACE = "-------------------------------";

    @Bean(name = "production")
    public boolean getProductionStatus() {
        return "prod".equals(profile);
    }

    @Bean(name = "nvfVersion")
    public iNAppInformation nvfVersion() {
        iNVersion version = new NVersionImpl(dateOfRelease, productName, productVersion, productBuild);
        return new NAppInformationImpl(creator, company, email, webpage, version);
    }

    @Bean
    public ServletRegistrationBean exampleServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new WebDownloadAsyncServlet(), "/DownloadServlet/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
