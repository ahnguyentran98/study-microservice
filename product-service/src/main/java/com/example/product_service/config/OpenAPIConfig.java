package com.example.product_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .description("Microservice for managing product catalog, inventory, and search functionality")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Product Service Team")
                                .email("product-service@example.com")));
    }
}
