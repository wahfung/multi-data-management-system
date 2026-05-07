package com.datasource.controller;

import com.datasource.dto.Result;
import com.datasource.dto.SyncRequest;
import com.datasource.entity.DataSyncRecord;
import com.datasource.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "数据同步")
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncController {

    private final DataSyncService dataSyncService;

    @Operation(summary = "执行数据同步")
    @PostMapping
    public Result<DataSyncRecord> sync(@Valid @RequestBody SyncRequest request, Authentication authentication) {
        String operatorName = authentication != null ? authentication.getName() : "system";
        return Result.success(dataSyncService.syncData(request, operatorName));
    }

    @Operation(summary = "拉取数据")
    @PostMapping("/pull")
    public Result<DataSyncRecord> pull(
            @RequestParam Long vendorId,
            @RequestParam(required = false) String endpoint,
            Authentication authentication) {
        String operatorName = authentication != null ? authentication.getName() : "system";
        return Result.success(dataSyncService.pullData(vendorId, endpoint, operatorName));
    }

    @Operation(summary = "推送数据")
    @PostMapping("/push")
    public Result<DataSyncRecord> push(
            @RequestParam Long vendorId,
            @RequestParam(required = false) String endpoint,
            @RequestBody Object data,
            Authentication authentication) {
        String operatorName = authentication != null ? authentication.getName() : "system";
        return Result.success(dataSyncService.pushData(vendorId, endpoint, data, operatorName));
    }
}
