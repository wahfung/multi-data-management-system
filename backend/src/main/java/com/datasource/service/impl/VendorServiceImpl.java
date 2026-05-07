package com.datasource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datasource.dto.VendorDTO;
import com.datasource.entity.DataSourceVendor;
import com.datasource.mapper.DataSourceVendorMapper;
import com.datasource.service.VendorService;
import com.datasource.service.adapter.DataSourceAdapterFactory;
import com.datasource.service.adapter.DataSourceAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorServiceImpl extends ServiceImpl<DataSourceVendorMapper, DataSourceVendor> implements VendorService {

    private final DataSourceAdapterFactory adapterFactory;

    @Override
    public IPage<DataSourceVendor> pageList(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<DataSourceVendor> wrapper = new LambdaQueryWrapper<DataSourceVendor>()
                .eq(DataSourceVendor::getDeleted, 0)
                .orderByDesc(DataSourceVendor::getCreateTime);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(DataSourceVendor::getVendorName, keyword)
                    .or()
                    .like(DataSourceVendor::getVendorCode, keyword));
        }

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<DataSourceVendor> listEnabled() {
        return list(new LambdaQueryWrapper<DataSourceVendor>()
                .eq(DataSourceVendor::getStatus, 1)
                .eq(DataSourceVendor::getDeleted, 0)
                .orderByAsc(DataSourceVendor::getVendorName));
    }

    @Override
    public DataSourceVendor createVendor(VendorDTO dto) {
        DataSourceVendor existing = getOne(new LambdaQueryWrapper<DataSourceVendor>()
                .eq(DataSourceVendor::getVendorCode, dto.getVendorCode())
                .eq(DataSourceVendor::getDeleted, 0));

        if (existing != null) {
            throw new IllegalArgumentException("厂商编码已存在");
        }

        DataSourceVendor vendor = new DataSourceVendor();
        vendor.setVendorCode(dto.getVendorCode());
        vendor.setVendorName(dto.getVendorName());
        vendor.setApiBaseUrl(dto.getApiBaseUrl());
        vendor.setApiVersion(dto.getApiVersion());
        vendor.setAuthType(dto.getAuthType());
        vendor.setAuthConfig(dto.getAuthConfig());
        vendor.setAdapterType(dto.getAdapterType());
        vendor.setDescription(dto.getDescription());
        vendor.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        vendor.setDeleted(0);

        save(vendor);

        log.info("创建数据源厂商: {}", vendor.getVendorName());

        return vendor;
    }

    @Override
    public DataSourceVendor updateVendor(VendorDTO dto) {
        DataSourceVendor vendor = getById(dto.getId());
        if (vendor == null) {
            throw new IllegalArgumentException("厂商不存在");
        }

        DataSourceVendor existing = getOne(new LambdaQueryWrapper<DataSourceVendor>()
                .eq(DataSourceVendor::getVendorCode, dto.getVendorCode())
                .ne(DataSourceVendor::getId, dto.getId())
                .eq(DataSourceVendor::getDeleted, 0));

        if (existing != null) {
            throw new IllegalArgumentException("厂商编码已存在");
        }

        vendor.setVendorCode(dto.getVendorCode());
        vendor.setVendorName(dto.getVendorName());
        vendor.setApiBaseUrl(dto.getApiBaseUrl());
        vendor.setApiVersion(dto.getApiVersion());
        vendor.setAuthType(dto.getAuthType());
        vendor.setAuthConfig(dto.getAuthConfig());
        vendor.setAdapterType(dto.getAdapterType());
        vendor.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            vendor.setStatus(dto.getStatus());
        }

        updateById(vendor);

        log.info("更新数据源厂商: {}", vendor.getVendorName());

        return vendor;
    }

    @Override
    public boolean testConnection(Long vendorId) {
        DataSourceVendor vendor = getById(vendorId);
        if (vendor == null) {
            throw new IllegalArgumentException("厂商不存在");
        }

        DataSourceAdapter adapter = adapterFactory.getAdapter(vendor);
        boolean result = adapter.testConnection(vendor);

        log.info("测试厂商连接: {} - {}", vendor.getVendorName(), result ? "成功" : "失败");

        return result;
    }
}
