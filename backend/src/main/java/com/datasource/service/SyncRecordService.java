package com.datasource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.datasource.dto.RecordQueryDTO;
import com.datasource.entity.DataSyncRecord;

import java.util.Map;

public interface SyncRecordService extends IService<DataSyncRecord> {

    IPage<DataSyncRecord> pageQuery(RecordQueryDTO query);

    DataSyncRecord getDetail(Long id);

    Map<String, Object> getStatistics();
}
