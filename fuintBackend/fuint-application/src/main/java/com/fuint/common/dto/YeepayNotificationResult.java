package com.fuint.common.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 易宝支付通知处理结果
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@Getter
@Setter
public class YeepayNotificationResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 商户订单号
     */
    private String orderId;
    
    /**
     * 易宝交易ID
     */
    private String uniqueOrderNo;
    
    /**
     * 支付状态
     */
    private String status;
    
    /**
     * 支付金额
     */
    private Double payAmount;
    
    /**
     * 支付时间
     */
    private String paySuccessDate;
    
    /**
     * 是否已处理过
     */
    private boolean processed;
}