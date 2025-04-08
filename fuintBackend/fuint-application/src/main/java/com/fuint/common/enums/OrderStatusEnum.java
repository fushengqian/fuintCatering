package com.fuint.common.enums;

import com.fuint.common.dto.ParamDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单状态枚举
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public enum OrderStatusEnum {
    CREATED("A", "待支付"),
    PAID("B", "已支付"),
    CANCEL("C", "已取消"),
    DELIVERY("D", "待发货"),
    DELIVERED("E", "已发货"),
    RECEIVED("F", "已收货"),
    DELETED("G", "已删除"),
    REFUND("H", "已退款");

    private String key;

    private String value;

    OrderStatusEnum(String key, String value) {
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

    public static List<ParamDto> getOrderStatusList() {
        return Arrays.stream(OrderStatusEnum.values())
                .map(status -> new ParamDto(status.getKey(), status.getValue(), status.getValue()))
                .collect(Collectors.toList());
    }
}
