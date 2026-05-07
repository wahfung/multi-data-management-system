package com.datasource.util;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HttpClientUtil {

    private final CloseableHttpClient httpClient;

    public HttpClientUtil() {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(30, TimeUnit.SECONDS))
                .setResponseTimeout(Timeout.of(60, TimeUnit.SECONDS))
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

    public HttpResponse doGet(String url, Map<String, String> headers) {
        HttpGet request = new HttpGet(url);
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
        return execute(request);
    }

    public HttpResponse doPost(String url, Map<String, String> headers, Object body) {
        HttpPost request = new HttpPost(url);
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
        if (body != null) {
            String jsonBody = body instanceof String ? (String) body : JSON.toJSONString(body);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)));
        }
        return execute(request);
    }

    public HttpResponse doPut(String url, Map<String, String> headers, Object body) {
        HttpPut request = new HttpPut(url);
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
        if (body != null) {
            String jsonBody = body instanceof String ? (String) body : JSON.toJSONString(body);
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)));
        }
        return execute(request);
    }

    public HttpResponse doDelete(String url, Map<String, String> headers) {
        HttpDelete request = new HttpDelete(url);
        if (headers != null) {
            headers.forEach(request::addHeader);
        }
        return execute(request);
    }

    private HttpResponse execute(HttpUriRequestBase request) {
        long startTime = System.currentTimeMillis();
        try {
            return httpClient.execute(request, response -> {
                HttpResponse result = new HttpResponse();
                result.setStatusCode(response.getCode());
                result.setBody(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
                result.setExecutionTime(System.currentTimeMillis() - startTime);
                return result;
            });
        } catch (Exception e) {
            log.error("HTTP请求失败: {}", e.getMessage(), e);
            HttpResponse errorResponse = new HttpResponse();
            errorResponse.setStatusCode(500);
            errorResponse.setBody(e.getMessage());
            errorResponse.setExecutionTime(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    @lombok.Data
    public static class HttpResponse {
        private int statusCode;
        private String body;
        private long executionTime;
    }
}
