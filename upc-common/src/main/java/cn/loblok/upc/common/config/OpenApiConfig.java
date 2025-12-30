package cn.loblok.upc.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("UPC 社区系统 API")
                        .description("包含用户中心、社区讨论、交易模块的接口文档")
                        .version("v1.0.0"));
    }
}