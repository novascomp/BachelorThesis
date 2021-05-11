package me.novascomp.messages.config;

import me.novascomp.utils.logger.LoggerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoggerInterceptorConfig implements WebMvcConfigurer {

    public LoggerInterceptorConfig() {
        super();
    }

    @Bean
    public LoggerInterceptor interceptorBean() {
        return new LoggerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorBean());
    }
}
