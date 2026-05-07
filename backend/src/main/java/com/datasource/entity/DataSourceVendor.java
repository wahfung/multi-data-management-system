package com.datasource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ds_vendor")
public class DataSourceVendor {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String vendorCode;

    private String vendorName;

    private String apiBaseUrl;

    private String apiVersion;

    private String authType;

    private String authConfig;

    private String adapterType;

    private String description;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
