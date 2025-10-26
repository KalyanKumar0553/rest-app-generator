package com.src.main.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI apiInfo() {
    return new OpenAPI()
      .info(new Info()
        .title("REST App Generator API")
        .description("Generates Spring Boot REST scaffolds from YAML specification.")
        .version("1.0.0"));
  }
}
