package com.datasource.controller;

import com.datasource.dto.PageResult;
import com.datasource.dto.Result;
import com.datasource.dto.VendorDTO;
import com.datasource.entity.DataSourceVendor;
import com.datasource.service.VendorService;
import com.datasource.service.adapter.DataSourceAdapterFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据源厂商管理")
@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final DataSourceAdapterFactory adapterFactory;

    @Operation(summary = "分页查询厂商列表")
    @GetMapping("/page")
    public Result<PageResult<DataSourceVendor>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(vendorService.pageList(pageNum, pageSize, keyword)));
    }

    @Operation(summary = "获取启用的厂商列表")
    @GetMapping("/list")
    public Result<List<DataSourceVendor>> list() {
        return Result.success(vendorService.listEnabled());
    }

    @Operation(summary = "获取厂商详情")
    @GetMapping("/{id}")
    public Result<DataSourceVendor> getById(@PathVariable Long id) {
        return Result.success(vendorService.getById(id));
    }

    @Operation(summary = "创建厂商")
    @PostMapping
    public Result<DataSourceVendor> create(@Valid @RequestBody VendorDTO dto) {
        return Result.success(vendorService.createVendor(dto));
    }

    @Operation(summary = "更新厂商")
    @PutMapping
    public Result<DataSourceVendor> update(@Valid @RequestBody VendorDTO dto) {
        return Result.success(vendorService.updateVendor(dto));
    }

    @Operation(summary = "删除厂商")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        vendorService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "测试厂商连接")
    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        return Result.success(vendorService.testConnection(id));
    }

    @Operation(summary = "获取支持的适配器类型")
    @GetMapping("/adapter-types")
    public Result<List<String>> getAdapterTypes() {
        return Result.success(adapterFactory.getAvailableTypes());
    }
}
