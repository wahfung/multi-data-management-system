package com.datasource.service.adapter;

import com.datasource.entity.DataSourceVendor;
import java.util.Map;

public interface DataSourceAdapter {

    String getType();

    boolean testConnection(DataSourceVendor vendor);

    Map<String, String> buildHeaders(DataSourceVendor vendor, Map<String, String> customHeaders);

    int parseDataCount(String responseBody);

    Object normalizeResponse(String responseBody);
}
