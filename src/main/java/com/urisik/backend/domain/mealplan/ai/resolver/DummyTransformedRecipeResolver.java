package com.urisik.backend.domain.mealplan.ai.resolver;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DummyTransformedRecipeResolver implements TransformedRecipeResolver {

    private final AtomicLong seq = new AtomicLong(50000);
    private final Map<String, Long> store = new ConcurrentHashMap<>();

    @Override
    public Long resolveOrCreate(Long familyRoomId, Long recipeId) {
        String key = familyRoomId + ":" + recipeId;
        return store.computeIfAbsent(key, k -> seq.incrementAndGet());
    }
}
