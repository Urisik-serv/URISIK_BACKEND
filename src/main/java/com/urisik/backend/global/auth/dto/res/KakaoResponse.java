package com.urisik.backend.global.auth.dto.res;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        // kakao는 최상위에 id가 있음
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        // kakao_account 내부에 email이 있음
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");

        if (kakaoAccount == null || kakaoAccount.get("email") == null) {
            return null; // 이메일 권한 없거나 비공개일 수 있음
        }
        return kakaoAccount.get("email").toString();
    }

    @Override
    public String getName() {
        // kakao_account.profile.nickname
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        if (kakaoAccount == null) return null;

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null || profile.get("nickname") == null) {
            return null;
        }

        return profile.get("nickname").toString();
    }
}
