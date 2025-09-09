package com.fuint.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 易宝支付订单查询响应结果
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public class YeepayOrderQueryResponse implements Serializable {

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
     * 订单状态：PROCESSING（处理中）、SUCCESS（成功）、FAILED（失败）
     */
    private String status;

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
     * 支付时间
     */
    private Date payTime;

    /**
     * 支付方式
     */
    private String payWay;

    /**
     * 支付渠道
     */
    private String payChannel;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    public String getPayChannel() {
        return payChannel;
    }

    public void setPayChannel(String payChannel) {
        this.payChannel = payChannel;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
}