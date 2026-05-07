package com.datasource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datasource.dto.RecordQueryDTO;
import com.datasource.entity.DataSyncRecord;
import com.datasource.mapper.DataSyncRecordMapper;
import com.datasource.service.SyncRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncRecordServiceImpl extends ServiceImpl<DataSyncRecordMapper, DataSyncRecord> implements SyncRecordService {

    @Override
    public IPage<DataSyncRecord> pageQuery(RecordQueryDTO query) {
        LambdaQueryWrapper<DataSyncRecord> wrapper = new LambdaQueryWrapper<DataSyncRecord>()
                .orderByDesc(DataSyncRecord::getCreateTime);

        if (query.getVendorId() != null) {
            wrapper.eq(DataSyncRecord::getVendorId, query.getVendorId());
        }

        if (query.getOperationType() != null) {
            wrapper.eq(DataSyncRecord::getOperationType, query.getOperationType());
        }

        if (query.getSyncStatus() != null) {
            wrapper.eq(DataSyncRecord::getSyncStatus, query.getSyncStatus());
        }

        if (query.getStartTime() != null) {
            wrapper.ge(DataSyncRecord::getCreateTime, query.getStartTime());
        }

        if (query.getEndTime() != null) {
            wrapper.le(DataSyncRecord::getCreateTime, query.getEndTime());
        }

        return page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public DataSyncRecord getDetail(Long id) {
        return getById(id);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalCount = count();
        long successCount = count(new LambdaQueryWrapper<DataSyncRecord>()
                .eq(DataSyncRecord::getSyncStatus, 1));
        long failCount = count(new LambdaQueryWrapper<DataSyncRecord>()
                .eq(DataSyncRecord::getSyncStatus, 2));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = count(new LambdaQueryWrapper<DataSyncRecord>()
                .ge(DataSyncRecord::getCreateTime, todayStart));

        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("failCount", failCount);
        stats.put("todayCount", todayCount);
        stats.put("successRate", totalCount > 0 ? String.format("%.2f", successCount * 100.0 / totalCount) + "%" : "0%");

        return stats;
    }
}
