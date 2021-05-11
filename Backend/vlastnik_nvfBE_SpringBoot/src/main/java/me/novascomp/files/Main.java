package me.novascomp.files;

import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.files.config.BeansInit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"me.novascomp.files"})
@EnableJpaRepositories
@EnableTransactionManagement
@EntityScan(basePackages = {"me.novascomp.files.model"})
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    static public void main(String[] args) {
        SpringApplication application = new SpringApplication(Main.class);
        application.run(args);
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        LOG.log(Level.INFO, "WELCOME TO NVF");
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
    }
}
