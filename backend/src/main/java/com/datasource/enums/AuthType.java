package com.datasource.enums;

import lombok.Getter;

@Getter
public enum AuthType {
    NONE("none", "无认证"),
    API_KEY("api_key", "API密钥"),
    BASIC("basic", "Basic认证"),
    BEARER("bearer", "Bearer Token"),
    OAUTH2("oauth2", "OAuth2.0");

    private final String code;
    private final String desc;

    AuthType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AuthType fromCode(String code) {
        for (AuthType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return NONE;
    }
}
