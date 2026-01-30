package com.urisik.backend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
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


    // 개발 환경에서는 아래 openAPI를 아예 삭제로, EC2배포에서는 "https://api.urisik.com"로 설정해야함.
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://api.urisik.com"));
    }

    /*
     * 각 API 도메인 별로 그룹화하여 Swagger UI에 표시합니다.
     * */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01. auth")
                .displayName("01. 인증 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch("/api/auth/**") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
                .group("02. member")
                .displayName("02. 사용자 동의여부 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch("/api/member/**") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi recipeApi() {
        return GroupedOpenApi.builder()
                .group("03. recipe")
                .displayName("03. 레시피 API")
                .pathsToMatch("/api/recipes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi familyMemberProfileApi() {
        return GroupedOpenApi.builder()
                .group("04. profile")
                .displayName("04. 프로필 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch("/api/family-rooms/*/profiles/**",
                        "/api/family-rooms/*/profile-pic") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }

    @Bean
    public GroupedOpenApi familyRoomApi() {
        return GroupedOpenApi.builder()
                .group("05. familyRoom")
                .displayName("05. 가족방 API")
                .pathsToMatch("/api/family-rooms",
                        "/api/family-rooms/me")
                .build();
    }

    @Bean
    public GroupedOpenApi wishListApi() {
        return GroupedOpenApi.builder()
                .group("06. wishList")
                .displayName("06. 개인 위시리스트 API")     // UI 드롭다운에 보일 이름
                .pathsToMatch("/api/family-rooms/*/profile-wishes") // 이 그룹에 포함될 API의 URL 패턴
                .build();
    }
  
    @Bean
    public GroupedOpenApi inviteApi() {
        return GroupedOpenApi.builder()
                .group("07. invites")
                .displayName("07. 초대 API")
                .pathsToMatch("/api/family-rooms/{familyRoomId}/invites",
                        "/api/invites/**")
                .build();
    }

    @Bean
    public GroupedOpenApi familyWishListApi() {
        return GroupedOpenApi.builder()
                .group("08. familyWishList")
                .displayName("08. 가족 위시리스트 API")
                .pathsToMatch("/api/family-rooms/{familyRoomId}/family-wishlist/**")
                .build();
    }

    @Bean
    public GroupedOpenApi MealPlanApi() {
        return GroupedOpenApi.builder()
                .group("09. mealPlan")
                .displayName("09. 식단 API")
                .pathsToMatch("/api/family-rooms/{familyRoomId}/meal-plans/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("10. notification")
                .displayName("10. 알림 API")
                .pathsToMatch("/api/notifications/**")
                .build();
    }


    // 아래 부분 지우고 각자 도메인에 맞게 변경하면 됨!!
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00. ALL")
                .displayName("00. 전체 API")
                .pathsToMatch("/**") // 모든 경로 포함
                .build();
    }


}
