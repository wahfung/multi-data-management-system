package com.datasource.service.adapter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.datasource.entity.DataSourceVendor;
import com.datasource.enums.AuthType;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractDataSourceAdapter implements DataSourceAdapter {

    @Override
    public Map<String, String> buildHeaders(DataSourceVendor vendor, Map<String, String> customHeaders) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Accept", "application/json");
        headers.put("Accept-Charset", "UTF-8");

        AuthType authType = AuthType.fromCode(vendor.getAuthType());
        String authConfig = vendor.getAuthConfig();

        if (authConfig != null && !authConfig.isEmpty()) {
            JSONObject config = JSON.parseObject(authConfig);

            switch (authType) {
                case API_KEY:
                    String headerName = config.getString("headerName");
                    String apiKey = config.getString("apiKey");
                    if (headerName != null && apiKey != null) {
                        headers.put(headerName, apiKey);
                    }
                    break;

                case BASIC:
                    String username = config.getString("username");
                    String password = config.getString("password");
                    if (username != null && password != null) {
                        String credentials = username + ":" + password;
                        String encoded = Base64.getEncoder().encodeToString(
                                credentials.getBytes(StandardCharsets.UTF_8));
                        headers.put("Authorization", "Basic " + encoded);
                    }
                    break;

                case BEARER:
                    String token = config.getString("token");
                    if (token != null) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    break;

                case OAUTH2:
                    String accessToken = config.getString("accessToken");
                    if (accessToken != null) {
                        headers.put("Authorization", "Bearer " + accessToken);
                    }
                    break;

                default:
                    break;
            }
        }

        if (customHeaders != null) {
            headers.putAll(customHeaders);
        }

        return headers;
    }

    @Override
    public int parseDataCount(String responseBody) {
        try {
            if (responseBody == null || responseBody.isEmpty()) {
                return 0;
            }

            Object parsed = JSON.parse(responseBody);

            if (parsed instanceof JSONArray) {
                return ((JSONArray) parsed).size();
            }

            if (parsed instanceof JSONObject jsonObject) {
                if (jsonObject.containsKey("data")) {
                    Object data = jsonObject.get("data");
                    if (data instanceof JSONArray) {
                        return ((JSONArray) data).size();
                    }
                }

                if (jsonObject.containsKey("total")) {
                    return jsonObject.getIntValue("total");
                }

                if (jsonObject.containsKey("count")) {
                    return jsonObject.getIntValue("count");
                }

                return 1;
            }

            return 0;
        } catch (Exception e) {
            log.warn("解析数据量失败: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public Object normalizeResponse(String responseBody) {
        try {
            return JSON.parse(responseBody);
        } catch (Exception e) {
            return responseBody;
        }
    }
}
