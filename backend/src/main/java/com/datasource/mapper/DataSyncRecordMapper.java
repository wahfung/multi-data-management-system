package com.datasource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datasource.entity.DataSyncRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataSyncRecordMapper extends BaseMapper<DataSyncRecord> {
}
