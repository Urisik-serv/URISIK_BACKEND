package com.urisik.backend.domain.recipe.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@EnableConfigurationProperties(FoodSafetyProperties.class)
public class FoodSafetyConfig {

    @Bean
    public WebClient foodSafetyWebClient(FoodSafetyProperties props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }

}
