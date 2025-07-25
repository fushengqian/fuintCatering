package com.fuint.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 易宝支付响应结果
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public class YeepayPaymentResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回码，SUCCESS表示成功，其他表示失败
     */
    private String code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 预支付标识（二维码数据）
     */
    private String prePayTn;

    /**
     * 易宝支付唯一订单号
     */
    private String uniqueOrderNo;

    /**
     * 商户订单号
     */
    private String orderId;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 支付链接（用于跳转支付）
     */
    private String payLink;

    /**
     * 支付状态
     */
    private String status;

    /**
     * 原始响应数据
     */
    private String rawResponse;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPrePayTn() {
        return prePayTn;
    }

    public void setPrePayTn(String prePayTn) {
        this.prePayTn = prePayTn;
    }

    public String getUniqueOrderNo() {
        return uniqueOrderNo;
    }

    public void setUniqueOrderNo(String uniqueOrderNo) {
        this.uniqueOrderNo = uniqueOrderNo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getPayLink() {
        return payLink;
    }

    public void setPayLink(String payLink) {
        this.payLink = payLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}