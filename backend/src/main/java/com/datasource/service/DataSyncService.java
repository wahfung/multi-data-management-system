package com.datasource.service;

import com.datasource.dto.SyncRequest;
import com.datasource.entity.DataSyncRecord;

public interface DataSyncService {

    DataSyncRecord syncData(SyncRequest request, String operatorName);

    DataSyncRecord pullData(Long vendorId, String endpoint, String operatorName);

    DataSyncRecord pushData(Long vendorId, String endpoint, Object data, String operatorName);
}
