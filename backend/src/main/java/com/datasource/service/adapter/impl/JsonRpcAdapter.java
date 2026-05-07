package com.datasource.service.adapter.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.datasource.entity.DataSourceVendor;
import com.datasource.service.adapter.AbstractDataSourceAdapter;
import com.datasource.util.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonRpcAdapter extends AbstractDataSourceAdapter {

    private final HttpClientUtil httpClientUtil;

    @Override
    public String getType() {
        return "json_rpc";
    }

    @Override
    public boolean testConnection(DataSourceVendor vendor) {
        try {
            Map<String, String> headers = buildHeaders(vendor, null);

            Map<String, Object> rpcRequest = new HashMap<>();
            rpcRequest.put("jsonrpc", "2.0");
            rpcRequest.put("method", "ping");
            rpcRequest.put("params", new Object[]{});
            rpcRequest.put("id", 1);

            HttpClientUtil.HttpResponse response = httpClientUtil.doPost(
                    vendor.getApiBaseUrl(), headers, rpcRequest);

            return response.getStatusCode() >= 200 && response.getStatusCode() < 400;
        } catch (Exception e) {
            log.error("JSON-RPC测试连接失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int parseDataCount(String responseBody) {
        try {
            JSONObject json = JSON.parseObject(responseBody);

            if (json.containsKey("result")) {
                Object result = json.get("result");
                if (result instanceof com.alibaba.fastjson2.JSONArray) {
                    return ((com.alibaba.fastjson2.JSONArray) result).size();
                }
            }

            return super.parseDataCount(responseBody);
        } catch (Exception e) {
            return super.parseDataCount(responseBody);
        }
    }
}
