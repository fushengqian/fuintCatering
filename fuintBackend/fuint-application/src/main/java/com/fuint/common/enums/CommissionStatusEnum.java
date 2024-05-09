package com.fuint.common.enums;

/**
 * 分佣状态
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum CommissionStatusEnum {
    NORMAL("A", "待结算"),
    SETTLED("B", "已结算"),
    CANCEL("C", "已作废");

    private String key;

    private String value;

    CommissionStatusEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
