package com.urisik.backend.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /*
     * 각 API 도메인 별로 그룹화하여 Swagger UI에 표시합니다.
     * */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("1. 인증 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch("/api/auth/**") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi recipeApi() {
        return GroupedOpenApi.builder()
                .group("recipe")
                .displayName("2. 레시피 API")
                .pathsToMatch("/api/recipes/**")
                .build();
    }


}
