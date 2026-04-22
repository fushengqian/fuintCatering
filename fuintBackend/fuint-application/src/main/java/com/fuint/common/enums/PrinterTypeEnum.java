package com.fuint.common.enums;

/**
 * 打印机类型枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum PrinterTypeEnum {

    ORDER("order", "票据打印机"),
    LABEL("label", "标签打印机");

    private String key;

    private String value;

    PrinterTypeEnum(String key, String value) {
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
