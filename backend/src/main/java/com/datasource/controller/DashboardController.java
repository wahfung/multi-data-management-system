package com.datasource.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.datasource.dto.Result;
import com.datasource.entity.DataSourceVendor;
import com.datasource.entity.DataSyncRecord;
import com.datasource.service.SyncRecordService;
import com.datasource.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final VendorService vendorService;
    private final SyncRecordService syncRecordService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        long vendorCount = vendorService.count(new LambdaQueryWrapper<DataSourceVendor>()
                .eq(DataSourceVendor::getDeleted, 0));

        long activeVendorCount = vendorService.count(new LambdaQueryWrapper<DataSourceVendor>()
                .eq(DataSourceVendor::getStatus, 1)
                .eq(DataSourceVendor::getDeleted, 0));

        Map<String, Object> statistics = syncRecordService.getStatistics();

        dashboard.put("vendorCount", vendorCount);
        dashboard.put("activeVendorCount", activeVendorCount);
        dashboard.putAll(statistics);

        List<Map<String, Object>> weeklyTrend = getWeeklyTrend();
        dashboard.put("weeklyTrend", weeklyTrend);

        List<DataSyncRecord> recentRecords = syncRecordService.list(
                new LambdaQueryWrapper<DataSyncRecord>()
                        .orderByDesc(DataSyncRecord::getCreateTime)
                        .last("LIMIT 10"));
        dashboard.put("recentRecords", recentRecords);

        return Result.success(dashboard);
    }

    private List<Map<String, Object>> getWeeklyTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            long successCount = syncRecordService.count(new LambdaQueryWrapper<DataSyncRecord>()
                    .eq(DataSyncRecord::getSyncStatus, 1)
                    .ge(DataSyncRecord::getCreateTime, startOfDay)
                    .lt(DataSyncRecord::getCreateTime, endOfDay));

            long failCount = syncRecordService.count(new LambdaQueryWrapper<DataSyncRecord>()
                    .eq(DataSyncRecord::getSyncStatus, 2)
                    .ge(DataSyncRecord::getCreateTime, startOfDay)
                    .lt(DataSyncRecord::getCreateTime, endOfDay));

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("success", successCount);
            dayData.put("fail", failCount);
            trend.add(dayData);
        }

        return trend;
    }
}
