package com.fuint.common.service;

import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayOrderQueryResponse;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.repository.model.MtOrder;

/**
 * 易宝支付服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface YeepayService {

    /**
     * 发起易宝支付
     *
     * @param order 订单信息
     * @return 支付结果，包含预支付标识（二维码数据）
     * @throws Exception 支付异常
     */
    YeepayPaymentResponse initiatePayment(MtOrder order) throws Exception;

    /**
     * 查询易宝支付订单状态
     *
     * @param orderId 商户订单号
     * @return 订单查询结果，包含支付状态和交易ID
     * @throws Exception 查询异常
     */
    YeepayOrderQueryResponse queryOrder(String orderId) throws Exception;
    
    /**
     * 处理易宝支付通知
     *
     * @param notificationData 通知数据
     * @return 处理结果
     * @throws Exception 处理异常
     */
    YeepayNotificationResult processNotification(String notificationData) throws Exception;
}