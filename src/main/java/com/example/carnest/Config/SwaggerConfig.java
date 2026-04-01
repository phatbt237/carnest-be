package com.example.carnest.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CarNest API")
                        .description("API cho marketplace xe mô hình")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token",
                                new SecurityScheme()
                                        .name("Bearer Token")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập access token vào đây (không cần ghi 'Bearer ' phía trước)")
                        ));
    }

    @Bean
    public PropertyCustomizer propertyCustomizer() {
        return (schema, type) -> {

            if ("integer".equals(schema.getType()) || "number".equals(schema.getType())) {
                schema.setExample(0);
            }

            return schema;
        };
    }
}