package com.userms.usermsbackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("海朋友匹配系统接口文档")
                        .description("海朋友匹配系统接口文档的详细描述")
                        .version("V1.0.0")
                        .contact(new Contact().name("嗨呀"))
                );
    }

}

