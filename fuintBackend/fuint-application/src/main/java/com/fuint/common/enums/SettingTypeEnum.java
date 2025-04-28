package com.fuint.common.enums;

/**
 * 配置类型枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum SettingTypeEnum {
    POINT("point", "积分配置"),
    BALANCE("balance", "余额配置"),
    USER("user", "会员配置"),
    ORDER("order", "交易配置"),
    SUB_MESSAGE("sub_message", "订阅消息"),
    PRINTER("printer", "打印设置"),
    SMS_CONFIG("sms_config", "短信配置"),
    KUAIDI100("kuaidi100", "快递100配置");

    private String key;

    private String value;

    SettingTypeEnum(String key, String value) {
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

    // 普通方法，通过key获取value
    public static String getValue(String k) {
        for (SettingTypeEnum c : SettingTypeEnum.values()) {
            if (c.getKey().equals(k)) {
                return c.getValue();
            }
        }
        return null;
    }

    // 普通方法，通过Value获取key
    public static String getKey(String v) {
        for (SettingTypeEnum c : SettingTypeEnum.values()) {
            if (c.getValue().equals(v)) {
                return c.getKey();
            }
        }
        return null;
    }
}
