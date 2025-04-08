package com.fuint.common.enums;

import com.fuint.common.dto.ParamDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付类型
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum PayTypeEnum {
    CASH("CASH", "现金支付"),
    JSAPI("JSAPI", "微信支付"),
    MICROPAY("MICROPAY", "微信扫码支付"),
    BALANCE("BALANCE", "余额支付"),
    ALISCAN("ALISCAN", "支付宝扫码"),
    STORE("STORE", "门店支付");

    private String key;

    private String value;

    PayTypeEnum(String key, String value) {
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

    public static List<ParamDto> getPayTypeList() {
        return Arrays.stream(PayTypeEnum.values())
                .map(status -> new ParamDto(status.getKey(), status.getValue(), status.getValue()))
                .collect(Collectors.toList());
    }
}
