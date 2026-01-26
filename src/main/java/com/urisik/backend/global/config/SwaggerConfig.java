package com.urisik.backend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "URISIK API", version = "v1"),
        security = { @SecurityRequirement(name = "bearerAuth") } // ✅ 전체 API에 기본 적용
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
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

    // 아래 부분 지우고 각자 도메인에 맞게 변경하면 됨!!
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0. ALL")
                .displayName("전체 API")
                .pathsToMatch("/**") // 모든 경로 포함
                .build();
    }


}
