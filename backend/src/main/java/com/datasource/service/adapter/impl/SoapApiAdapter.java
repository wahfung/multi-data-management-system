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
public class SoapApiAdapter extends AbstractDataSourceAdapter {

    private final HttpClientUtil httpClientUtil;

    @Override
    public String getType() {
        return "soap";
    }

    @Override
    public Map<String, String> buildHeaders(DataSourceVendor vendor, Map<String, String> customHeaders) {
        Map<String, String> headers = super.buildHeaders(vendor, customHeaders);
        headers.put("Content-Type", "text/xml; charset=UTF-8");
        headers.put("SOAPAction", "");
        return headers;
    }

    @Override
    public boolean testConnection(DataSourceVendor vendor) {
        try {
            Map<String, String> headers = buildHeaders(vendor, null);
            String wsdlUrl = vendor.getApiBaseUrl();
            if (!wsdlUrl.endsWith("?wsdl")) {
                wsdlUrl += "?wsdl";
            }
            HttpClientUtil.HttpResponse response = httpClientUtil.doGet(wsdlUrl, headers);
            return response.getStatusCode() >= 200 && response.getStatusCode() < 400;
        } catch (Exception e) {
            log.error("SOAP测试连接失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int parseDataCount(String responseBody) {
        return 1;
    }
}
