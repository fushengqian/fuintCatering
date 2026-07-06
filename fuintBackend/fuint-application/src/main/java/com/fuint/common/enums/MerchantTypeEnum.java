package com.fuint.common.enums;

import com.fuint.common.dto.common.ParamDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商户类型枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum MerchantTypeEnum {
    RESTAURANT("restaurant", "餐厅"),
    TEA_DRINK("teaDrink", "茶饮"),
    HOT_POT("hotPot", "火锅"),
    FAST_FOOD("fastFood", "快餐"),
    OTHER("other", "其他");

    private String key;

    private String value;

    MerchantTypeEnum(String key, String value) {
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

    public static List<ParamDto> getMerchantTypeList() {
        return Arrays.stream(MerchantTypeEnum.values())
                .map(status -> new ParamDto(status.getKey(), status.getValue(), status.getValue()))
                .collect(Collectors.toList());
    }
}
