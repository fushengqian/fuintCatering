package com.fuint.common.enums;

import com.fuint.common.dto.ParamDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 桌码使用状态
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum TableUseStatusEnum {

    AVAILABLE("A", "未开台"),
    TAKEN("B", "已开台"),
    DURING("C", "就餐中");

    private String key;

    private String value;

    TableUseStatusEnum(String key, String value) {
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

    public static List<ParamDto> getTableUseStatusList() {
        return Arrays.stream(TableUseStatusEnum.values())
                .map(status -> new ParamDto(status.getKey(), status.getValue(), status.getValue()))
                .collect(Collectors.toList());
    }
}
