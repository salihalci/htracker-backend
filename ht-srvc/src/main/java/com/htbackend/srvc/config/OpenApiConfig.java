package com.htbackend.srvc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI habitTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Habit Tracker API")
                        .description("API for tracking daily habits and their completions")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HT Backend Team")
                                .email("dev@htbackend.com")));
    }
}