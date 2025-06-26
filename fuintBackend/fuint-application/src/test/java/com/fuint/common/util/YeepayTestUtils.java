package com.fuint.common.util;

import com.alibaba.fastjson.JSONObject;
import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayOrderQueryResponse;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.repository.model.MtOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 易宝支付测试工具类
 * 提供模拟数据生成功能
 */
public class YeepayTestUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成模拟的MtOrder对象
     * @param orderId 订单ID
     * @param amount 订单金额
     * @param status 订单状态
     * @return 模拟的MtOrder对象
     */
    public static MtOrder createMockOrder(Integer orderId, BigDecimal amount, String status) {
        MtOrder order = new MtOrder();
        // 使用反射设置属性，因为模型类可能没有公共setter
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "orderSn", "ORDER_" + orderId);
        ReflectionTestUtils.setField(order, "amount", amount);
        ReflectionTestUtils.setField(order, "payAmount", amount);
        ReflectionTestUtils.setField(order, "status", status);
        ReflectionTestUtils.setField(order, "createTime", new Date());
        ReflectionTestUtils.setField(order, "updateTime", new Date());
        return order;
    }

    /**
     * 生成模拟的易宝支付请求响应
     * @param orderId 商户订单号
     * @param amount 订单金额
     * @return 模拟的YeepayPaymentResponse
     */
    public static YeepayPaymentResponse createMockPaymentResponse(String orderId, BigDecimal amount) {
        YeepayPaymentResponse response = new YeepayPaymentResponse();
        response.setCode("SUCCESS");
        response.setMessage("支付请求成功");
        response.setPrePayTn("YOP_TN_" + UUID.randomUUID().toString().replace("-", ""));
        response.setUniqueOrderNo("YOP_ORDER_" + UUID.randomUUID().toString().replace("-", ""));
        response.setOrderId(orderId);
        response.setOrderAmount(amount);
        response.setStatus("PROCESSING");
        response.setPayLink("https://sandbox.yeepay.com/cashier?tn=" + response.getPrePayTn());
        return response;
    }

    /**
     * 生成模拟的易宝订单查询响应
     * @param orderId 商户订单号
     * @param uniqueOrderNo 易宝交易ID
     * @param amount 订单金额
     * @param status 支付状态
     * @return 模拟的YeepayOrderQueryResponse
     */
    public static YeepayOrderQueryResponse createMockOrderQueryResponse(String orderId, String uniqueOrderNo, BigDecimal amount, String status) {
        YeepayOrderQueryResponse response = new YeepayOrderQueryResponse();
        response.setCode("SUCCESS");
        response.setMessage("查询成功");
        response.setUniqueOrderNo(uniqueOrderNo);
        response.setOrderId(orderId);
        response.setOrderAmount(amount);
        response.setStatus(status);
        
        // 将字符串日期转换为Date对象
        try {
            response.setPayTime(DATE_FORMAT.parse("2025-05-08 03:00:00"));
        } catch (ParseException e) {
            response.setPayTime(new Date()); // 如果解析失败，使用当前时间
        }
        
        response.setPayWay("SCAN_CODE");
        response.setPayChannel("WECHAT");
        return response;
    }

    /**
     * 生成模拟的易宝支付通知数据 (JSON字符串)
     * @param orderId 商户订单号
     * @param uniqueOrderNo 易宝交易ID
     * @param status 支付状态
     * @param payAmount 支付金额
     * @return 模拟的通知数据JSON字符串
     */
    public static String createMockNotificationData(String orderId, String uniqueOrderNo, String status, Double payAmount) {
        JSONObject notification = new JSONObject();
        notification.put("orderId", orderId);
        notification.put("uniqueOrderNo", uniqueOrderNo);
        notification.put("status", status);
        notification.put("payAmount", payAmount);
        notification.put("paySuccessDate", "2025-05-08 03:00:00"); // Example pay success date
        // Add other relevant fields as needed for testing
        return notification.toJSONString();
    }

    /**
     * 生成模拟的易宝支付通知处理结果
     * @param success 是否成功
     * @param message 消息
     * @param orderId 商户订单号
     * @param uniqueOrderNo 易宝交易ID
     * @param status 支付状态
     * @param payAmount 支付金额
     * @param paySuccessDate 支付时间
     * @param processed 是否已处理过
     * @return 模拟的YeepayNotificationResult
     */
    public static YeepayNotificationResult createMockNotificationResult(boolean success, String message, String orderId, String uniqueOrderNo, String status, Double payAmount, String paySuccessDate, boolean processed) {
        YeepayNotificationResult result = new YeepayNotificationResult();
        // 使用反射设置属性，避免使用setter方法
        ReflectionTestUtils.setField(result, "success", success);
        ReflectionTestUtils.setField(result, "message", message);
        ReflectionTestUtils.setField(result, "orderId", orderId);
        ReflectionTestUtils.setField(result, "uniqueOrderNo", uniqueOrderNo);
        ReflectionTestUtils.setField(result, "status", status);
        ReflectionTestUtils.setField(result, "payAmount", payAmount);
        ReflectionTestUtils.setField(result, "paySuccessDate", paySuccessDate);
        ReflectionTestUtils.setField(result, "processed", processed);
        return result;
    }
}