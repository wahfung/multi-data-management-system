package com.datasource.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class SyncRequest {
    @NotNull(message = "厂商ID不能为空")
    private Long vendorId;

    private String operationType;

    private String endpoint;

    private String method;

    private Map<String, String> headers;

    private Map<String, Object> params;

    private Object body;
}
