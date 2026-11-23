package com.ptit.training.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ptitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PTIT Student Training Management API")
                        .version("v1.0")
                        .description("RESTful API cho Hệ thống Quản lý Đào tạo Sinh viên PTIT"));
    }
}
