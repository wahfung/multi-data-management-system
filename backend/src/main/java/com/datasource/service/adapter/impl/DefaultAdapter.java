package com.datasource.service.adapter.impl;

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
public class DefaultAdapter extends AbstractDataSourceAdapter {

    private final HttpClientUtil httpClientUtil;

    @Override
    public String getType() {
        return "default";
    }

    @Override
    public boolean testConnection(DataSourceVendor vendor) {
        try {
            Map<String, String> headers = buildHeaders(vendor, null);
            HttpClientUtil.HttpResponse response = httpClientUtil.doGet(vendor.getApiBaseUrl(), headers);
            return response.getStatusCode() >= 200 && response.getStatusCode() < 400;
        } catch (Exception e) {
            log.error("测试连接失败: {}", e.getMessage());
            return false;
        }
    }
}
