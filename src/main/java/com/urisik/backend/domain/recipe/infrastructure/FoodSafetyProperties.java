package com.urisik.backend.domain.recipe.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "food-safety")
public class FoodSafetyProperties {

    /**
     * 예: http://openapi.foodsafetykorea.go.kr/api
     */
    private String baseUrl;

    /**
     * 식약처에서 발급받은 인증키
     */
    private String apiKey;

    /**
     * 서비스명 (고정값)
     * COOKRCP01
     */
    private String serviceId = "COOKRCP01";

    /**
     * 응답 타입 (json 고정)
     */
    private String dataType = "json";

}
