package com.datasource.enums;

import lombok.Getter;

@Getter
public enum SyncStatus {
    PENDING(0, "待处理"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    PARTIAL(3, "部分成功");

    private final int code;
    private final String desc;

    SyncStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SyncStatus fromCode(int code) {
        for (SyncStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }
}
