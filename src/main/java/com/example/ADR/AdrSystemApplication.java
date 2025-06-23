package com.example.ADR;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class AdrSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdrSystemApplication.class, args);
    }

    @Configuration
    public class JacksonConfig {
        @Bean
        public Jackson2ObjectMapperBuilderCustomizer customizer() {
            return builder -> builder
                    .modules(new JavaTimeModule())
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }
}
