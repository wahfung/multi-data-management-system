package com.datasource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.datasource.dto.VendorDTO;
import com.datasource.entity.DataSourceVendor;

import java.util.List;

public interface VendorService extends IService<DataSourceVendor> {

    IPage<DataSourceVendor> pageList(int pageNum, int pageSize, String keyword);

    List<DataSourceVendor> listEnabled();

    DataSourceVendor createVendor(VendorDTO dto);

    DataSourceVendor updateVendor(VendorDTO dto);

    boolean testConnection(Long vendorId);
}
