package com.datasource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ds_sync_record")
public class DataSyncRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vendorId;

    private String vendorName;

    private String operationType;

    private String requestUrl;

    private String requestMethod;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String requestHeaders;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String requestBody;

    private Integer responseCode;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String responseBody;

    private Long executionTime;

    private Integer syncStatus;

    private String errorMessage;

    private Integer dataCount;

    private String operatorName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
