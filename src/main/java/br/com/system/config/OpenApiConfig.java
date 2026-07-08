package br.com.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("REST API from management system")
                        .version("v1.0")
                        .description("REST API from management system for small stores.")
                        .termsOfService("https://seusite.com/termos")
                        .contact(new Contact()
                                .name("Bernardo Leris")
                                .email("bernardo.leris1@email.com")
                                .url("https://seusite.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
