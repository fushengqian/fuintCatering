package com.fuint.common.enums;

import com.fuint.common.dto.ParamDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 升级会员等级条件枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum UserGradeCatchTypeEnum {
    INIT("init", "默认获取"),
    PAY("pay", "付费升级"),
    FREQUENCY("frequency", "累计消费次数升级"),
    AMOUNT("amount", "累积消费金额升级");

    private String key;

    private String value;

    UserGradeCatchTypeEnum(String key, String value) {
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

    public static List<ParamDto> getUserGradeCatchTypeList() {
        return Arrays.stream(UserGradeCatchTypeEnum.values())
                .map(status -> new ParamDto(status.getKey(), status.getValue(), status.getValue()))
                .collect(Collectors.toList());
    }
}
