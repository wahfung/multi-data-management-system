package com.datasource.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VendorDTO {
    private Long id;

    @NotBlank(message = "厂商编码不能为空")
    private String vendorCode;

    @NotBlank(message = "厂商名称不能为空")
    private String vendorName;

    @NotBlank(message = "API地址不能为空")
    private String apiBaseUrl;

    private String apiVersion;

    private String authType;

    private String authConfig;

    private String adapterType;

    private String description;

    private Integer status;
}
