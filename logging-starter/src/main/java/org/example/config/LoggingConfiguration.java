package org.example.config;

import org.example.aspect.LoggingAspect;
import org.example.service.LoggerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class LoggingConfiguration {

    @Bean
    public LoggerService loggerService() {
        return new LoggerService();
    }

    @Bean
    public LoggingAspect loggingAspect(LoggerService loggerService) {
        return new LoggingAspect(loggerService);
    }
}
