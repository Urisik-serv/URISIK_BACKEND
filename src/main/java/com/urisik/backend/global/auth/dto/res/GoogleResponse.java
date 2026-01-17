package com.urisik.backend.global.auth.dto.res;

import java.util.Map;

public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {

        this.attribute = attribute;
    }

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {

        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        Object email = attribute.get("email");
        return email == null ? null : email.toString();
    }

    @Override
    public String getName() {
        Object name = attribute.get("name");
        return name == null ? null : name.toString();
    }
}