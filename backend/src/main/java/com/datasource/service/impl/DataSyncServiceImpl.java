package com.datasource.service.impl;

import com.alibaba.fastjson2.JSON;
import com.datasource.dto.SyncRequest;
import com.datasource.entity.DataSourceVendor;
import com.datasource.entity.DataSyncRecord;
import com.datasource.enums.OperationType;
import com.datasource.enums.SyncStatus;
import com.datasource.mapper.DataSyncRecordMapper;
import com.datasource.service.DataSyncService;
import com.datasource.service.VendorService;
import com.datasource.service.adapter.DataSourceAdapter;
import com.datasource.service.adapter.DataSourceAdapterFactory;
import com.datasource.util.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncServiceImpl implements DataSyncService {

    private final VendorService vendorService;
    private final DataSyncRecordMapper recordMapper;
    private final DataSourceAdapterFactory adapterFactory;
    private final HttpClientUtil httpClientUtil;

    @Override
    @Transactional
    public DataSyncRecord syncData(SyncRequest request, String operatorName) {
        DataSourceVendor vendor = vendorService.getById(request.getVendorId());
        if (vendor == null) {
            throw new IllegalArgumentException("厂商不存在");
        }

        DataSourceAdapter adapter = adapterFactory.getAdapter(vendor);

        DataSyncRecord record = new DataSyncRecord();
        record.setVendorId(vendor.getId());
        record.setVendorName(vendor.getVendorName());
        record.setOperationType(request.getOperationType() != null ? request.getOperationType() : OperationType.SYNC.getCode());
        record.setOperatorName(operatorName);
        record.setCreateTime(LocalDateTime.now());

        String url = buildUrl(vendor.getApiBaseUrl(), request.getEndpoint());
        String method = request.getMethod() != null ? request.getMethod().toUpperCase() : "GET";

        record.setRequestUrl(url);
        record.setRequestMethod(method);
        record.setRequestHeaders(JSON.toJSONString(request.getHeaders()));
        record.setRequestBody(request.getBody() != null ? JSON.toJSONString(request.getBody()) : null);

        long startTime = System.currentTimeMillis();

        try {
            Map<String, String> headers = adapter.buildHeaders(vendor, request.getHeaders());

            HttpClientUtil.HttpResponse response;
            switch (method) {
                case "POST":
                    response = httpClientUtil.doPost(url, headers, request.getBody());
                    break;
                case "PUT":
                    response = httpClientUtil.doPut(url, headers, request.getBody());
                    break;
                case "DELETE":
                    response = httpClientUtil.doDelete(url, headers);
                    break;
                default:
                    response = httpClientUtil.doGet(url, headers);
            }

            record.setResponseCode(response.getStatusCode());
            record.setResponseBody(response.getBody());
            record.setExecutionTime(response.getExecutionTime());

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                record.setSyncStatus(SyncStatus.SUCCESS.getCode());
                int dataCount = adapter.parseDataCount(response.getBody());
                record.setDataCount(dataCount);

                log.info("数据同步成功: 厂商={}, 操作={}, 数据量={}",
                        vendor.getVendorName(), record.getOperationType(), dataCount);
            } else {
                record.setSyncStatus(SyncStatus.FAILED.getCode());
                record.setErrorMessage("HTTP错误: " + response.getStatusCode());

                log.warn("数据同步失败: 厂商={}, 状态码={}",
                        vendor.getVendorName(), response.getStatusCode());
            }

        } catch (Exception e) {
            record.setExecutionTime(System.currentTimeMillis() - startTime);
            record.setSyncStatus(SyncStatus.FAILED.getCode());
            record.setErrorMessage(e.getMessage());

            log.error("数据同步异常: 厂商={}, 错误={}", vendor.getVendorName(), e.getMessage(), e);
        }

        recordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public DataSyncRecord pullData(Long vendorId, String endpoint, String operatorName) {
        SyncRequest request = new SyncRequest();
        request.setVendorId(vendorId);
        request.setOperationType(OperationType.PULL.getCode());
        request.setEndpoint(endpoint);
        request.setMethod("GET");
        return syncData(request, operatorName);
    }

    @Override
    @Transactional
    public DataSyncRecord pushData(Long vendorId, String endpoint, Object data, String operatorName) {
        SyncRequest request = new SyncRequest();
        request.setVendorId(vendorId);
        request.setOperationType(OperationType.PUSH.getCode());
        request.setEndpoint(endpoint);
        request.setMethod("POST");
        request.setBody(data);
        return syncData(request, operatorName);
    }

    private String buildUrl(String baseUrl, String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return baseUrl;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return base + path;
    }
}
