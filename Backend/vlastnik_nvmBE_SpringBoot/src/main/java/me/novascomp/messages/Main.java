package me.novascomp.messages;

import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.messages.config.BeansInit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    static public void main(String[] args) {
        SpringApplication application = new SpringApplication(Main.class);
        application.run(args);
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
        LOG.log(Level.INFO, "WELCOME TO NVM");
        LOG.log(Level.INFO, BeansInit.DOT_SPACE);
    }
}
