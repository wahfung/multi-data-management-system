package com.datasource.controller;

import com.datasource.dto.PageResult;
import com.datasource.dto.RecordQueryDTO;
import com.datasource.dto.Result;
import com.datasource.entity.DataSyncRecord;
import com.datasource.service.SyncRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "操作记录管理")
@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
public class RecordController {

    private final SyncRecordService syncRecordService;

    @Operation(summary = "分页查询操作记录")
    @GetMapping("/page")
    public Result<PageResult<DataSyncRecord>> page(RecordQueryDTO query) {
        return Result.success(PageResult.of(syncRecordService.pageQuery(query)));
    }

    @Operation(summary = "获取记录详情")
    @GetMapping("/{id}")
    public Result<DataSyncRecord> getById(@PathVariable Long id) {
        return Result.success(syncRecordService.getDetail(id));
    }

    @Operation(summary = "获取统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(syncRecordService.getStatistics());
    }
}
