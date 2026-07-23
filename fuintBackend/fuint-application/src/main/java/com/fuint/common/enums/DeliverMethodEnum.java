package com.fuint.common.enums;

import com.fuint.common.dto.common.ParamDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配送方式枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum DeliverMethodEnum {
    EXPRESS("express", "快递配送"),
    RIDER("rider", "骑手配送");

    private String key;

    private String value;

    DeliverMethodEnum(String key, String value) {
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

    public static String getValue(String k) {
        for (DeliverMethodEnum c : DeliverMethodEnum.values()) {
            if (c.getKey().equals(k)) {
                return c.getValue();
            }
        }
        return null;
    }

    public static List<ParamDto> getDeliverMethodList() {
        return Arrays.stream(DeliverMethodEnum.values())
                .map(status -> new ParamDto(status.getKey(), status.getValue(), status.getValue()))
                .collect(Collectors.toList());
    }
}
