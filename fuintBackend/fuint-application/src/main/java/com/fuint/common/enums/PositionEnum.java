package com.fuint.common.enums;

import com.fuint.common.dto.ParamDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 广告位枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum PositionEnum {
    M_HOME_BANNER("m_home_banner", "移动端首页焦点图"),
    M_HOME_ADS("m_home_ads", "移动端首页广告");

    private String key;

    private String value;

    PositionEnum(String key, String value) {
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
        for (WxMessageEnum c : WxMessageEnum.values()) {
            if (c.getKey().equals(k)) {
                return c.getValue();
            }
        }
        return null;
    }

    // 普通方法，通过Value获取key
    public static String getKey(String v) {
        for (WxMessageEnum c : WxMessageEnum.values()) {
            if (c.getValue() == v) {
                return c.getKey();
            }
        }
        return null;
    }

    public static List<ParamDto> getPositionList() {
        return Arrays.stream(PositionEnum.values())
                .map(status -> new ParamDto(status.getKey(), status.getValue(), status.getValue()))
                .collect(Collectors.toList());
    }
}
