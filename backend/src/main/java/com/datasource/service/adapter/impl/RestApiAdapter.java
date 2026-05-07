package com.datasource.service.adapter.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.datasource.entity.DataSourceVendor;
import com.datasource.service.adapter.AbstractDataSourceAdapter;
import com.datasource.util.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestApiAdapter extends AbstractDataSourceAdapter {

    private final HttpClientUtil httpClientUtil;

    @Override
    public String getType() {
        return "rest_api";
    }

    @Override
    public boolean testConnection(DataSourceVendor vendor) {
        try {
            Map<String, String> headers = buildHeaders(vendor, null);
            HttpClientUtil.HttpResponse response = httpClientUtil.doGet(vendor.getApiBaseUrl(), headers);
            return response.getStatusCode() >= 200 && response.getStatusCode() < 400;
        } catch (Exception e) {
            log.error("REST API测试连接失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int parseDataCount(String responseBody) {
        try {
            JSONObject json = JSON.parseObject(responseBody);

            if (json.containsKey("records")) {
                return json.getJSONArray("records").size();
            }
            if (json.containsKey("items")) {
                return json.getJSONArray("items").size();
            }
            if (json.containsKey("list")) {
                return json.getJSONArray("list").size();
            }

            return super.parseDataCount(responseBody);
        } catch (Exception e) {
            return super.parseDataCount(responseBody);
        }
    }
}
