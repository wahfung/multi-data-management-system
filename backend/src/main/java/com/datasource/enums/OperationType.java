package com.datasource.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    PULL("pull", "拉取数据"),
    PUSH("push", "推送数据"),
    SYNC("sync", "双向同步"),
    QUERY("query", "查询数据"),
    TEST("test", "连接测试");

    private final String code;
    private final String desc;

    OperationType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
