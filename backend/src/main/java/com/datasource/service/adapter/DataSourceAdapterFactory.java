package com.datasource.service.adapter;

import com.datasource.entity.DataSourceVendor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataSourceAdapterFactory {

    private final Map<String, DataSourceAdapter> adapterMap = new HashMap<>();
    private final DataSourceAdapter defaultAdapter;

    public DataSourceAdapterFactory(List<DataSourceAdapter> adapters) {
        DataSourceAdapter foundDefault = null;

        for (DataSourceAdapter adapter : adapters) {
            adapterMap.put(adapter.getType(), adapter);
            if ("default".equals(adapter.getType())) {
                foundDefault = adapter;
            }
        }

        this.defaultAdapter = foundDefault;

        log.info("已注册数据源适配器: {}", adapterMap.keySet());
    }

    public DataSourceAdapter getAdapter(DataSourceVendor vendor) {
        String adapterType = vendor.getAdapterType();

        if (adapterType == null || adapterType.isEmpty()) {
            return defaultAdapter;
        }

        DataSourceAdapter adapter = adapterMap.get(adapterType);

        if (adapter == null) {
            log.warn("未找到适配器类型: {}, 使用默认适配器", adapterType);
            return defaultAdapter;
        }

        return adapter;
    }

    public List<String> getAvailableTypes() {
        return adapterMap.keySet().stream().sorted().toList();
    }
}
