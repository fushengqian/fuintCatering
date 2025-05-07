package com.fuint.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fuint.application.config.YopClientConfig;
import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayOrderQueryResponse;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.common.service.YeepayService;
import com.fuint.repository.mapper.MtYeepayNotificationLogMapper;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtYeepayNotificationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 易宝支付服务接口实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
public class YeepayServiceImpl implements YeepayService {

    private static final Logger logger = LoggerFactory.getLogger(YeepayServiceImpl.class);

    /**
     * 易宝支付配置
     */
    @Autowired
    private YopClientConfig yopClientConfig;
    
    /**
     * 易宝支付通知日志Mapper
     */
    @Autowired
    private MtYeepayNotificationLogMapper yeepayNotificationLogMapper;

    /**
     * 发起易宝支付
     *
     * @param order 订单信息
     * @return 支付结果，包含预支付标识（二维码数据）
     * @throws Exception 支付异常
     */
    @Override
    public YeepayPaymentResponse initiatePayment(MtOrder order) throws Exception {
        // 构建响应对象
        YeepayPaymentResponse paymentResponse = new YeepayPaymentResponse();
        
        try {
            // 使用反射获取订单信息
            String orderSn = getFieldValue(order, "orderSn");
            BigDecimal payAmount = getFieldValue(order, "payAmount");
            
            // 记录日志
            logger.info("发起易宝支付，订单号：{}，金额：{}", orderSn, payAmount);
            
            // 模拟调用易宝支付接口
            // 实际实现中，需要使用HTTP客户端调用易宝支付API
            
            // 设置响应信息
            paymentResponse.setCode("SUCCESS");
            paymentResponse.setMessage("支付请求成功");
            paymentResponse.setPrePayTn("YOP_TN_" + System.currentTimeMillis());
            paymentResponse.setUniqueOrderNo("YOP_ORDER_" + System.currentTimeMillis());
            paymentResponse.setOrderId(orderSn);
            paymentResponse.setOrderAmount(payAmount);
            paymentResponse.setStatus("PROCESSING");
            paymentResponse.setPayLink("https://pay.yeepay.com/cashier?tn=" + paymentResponse.getPrePayTn());
            
            return paymentResponse;
        } catch (Exception e) {
            // 记录异常
            logger.error("调用易宝支付接口异常: {}", e.getMessage(), e);
            
            // 设置错误响应
            paymentResponse.setCode("ERROR");
            paymentResponse.setMessage("支付请求异常：" + e.getMessage());
            
            return paymentResponse;
        }
    }

    /**
     * 查询易宝支付订单状态
     *
     * @param orderId 商户订单号
     * @return 订单查询结果，包含支付状态和交易ID
     * @throws Exception 查询异常
     */
    @Override
    public YeepayOrderQueryResponse queryOrder(String orderId) throws Exception {
        // 构建响应对象
        YeepayOrderQueryResponse queryResponse = new YeepayOrderQueryResponse();
        
        try {
            // 记录日志
            logger.info("查询易宝支付订单状态，订单号：{}", orderId);
            
            // 模拟调用易宝支付订单查询接口
            // 实际实现中，需要使用HTTP客户端调用易宝支付API
            
            // 设置响应信息
            queryResponse.setCode("SUCCESS");
            queryResponse.setMessage("查询成功");
            queryResponse.setStatus("SUCCESS"); // 可能的值：PROCESSING（处理中）、SUCCESS（成功）、FAILED（失败）
            queryResponse.setUniqueOrderNo("YOP_ORDER_" + System.currentTimeMillis());
            queryResponse.setOrderId(orderId);
            queryResponse.setOrderAmount(new BigDecimal("100.00"));
            queryResponse.setPayTime(new Date());
            queryResponse.setPayWay("SCAN_CODE");
            queryResponse.setPayChannel("WECHAT");
            
            return queryResponse;
        } catch (Exception e) {
            // 记录异常
            logger.error("查询易宝支付订单状态异常: {}", e.getMessage(), e);
            
            // 设置错误响应
            queryResponse.setCode("ERROR");
            queryResponse.setMessage("订单查询异常：" + e.getMessage());
            
            return queryResponse;
        }
    }
    
    /**
     * 处理易宝支付通知
     *
     * @param notificationData 通知数据
     * @return 处理结果
     * @throws Exception 处理异常
     */
    @Override
    public YeepayNotificationResult processNotification(String notificationData) throws Exception {
        // 构建响应对象
        YeepayNotificationResult result = new YeepayNotificationResult();
        
        try {
            // 记录日志
            logger.info("处理易宝支付通知，通知数据：{}", notificationData);
            
            // 1. 验证通知数据的签名
            boolean isSignValid = verifyNotificationSignature(notificationData);
            if (!isSignValid) {
                result.setSuccess(false);
                result.setMessage("通知签名验证失败");
                logger.warn("易宝支付通知签名验证失败，通知数据：{}", notificationData);
                return result;
            }
            
            // 2. 解析通知数据
            JSONObject notificationJson = parseNotificationData(notificationData);
            if (notificationJson == null) {
                result.setSuccess(false);
                result.setMessage("通知数据解析失败");
                logger.warn("易宝支付通知数据解析失败，通知数据：{}", notificationData);
                return result;
            }
            
            // 3. 提取关键信息
            String orderId = notificationJson.getString("orderId");
            String uniqueOrderNo = notificationJson.getString("uniqueOrderNo");
            String status = notificationJson.getString("status");
            Double payAmount = notificationJson.getDouble("payAmount");
            String paySuccessDate = notificationJson.getString("paySuccessDate");
            
            // 设置结果对象的基本信息
            result.setOrderId(orderId);
            result.setUniqueOrderNo(uniqueOrderNo);
            result.setStatus(status);
            result.setPayAmount(payAmount);
            result.setPaySuccessDate(paySuccessDate);
            
            // 4. 检查是否已处理过该通知（幂等性检查）
            boolean isProcessed = checkIfAlreadyProcessed(orderId, uniqueOrderNo);
            if (isProcessed) {
                result.setSuccess(true);
                result.setProcessed(true);
                result.setMessage("通知已处理过");
                logger.info("易宝支付通知已处理过，订单号：{}，易宝交易ID：{}", orderId, uniqueOrderNo);
                return result;
            }
            
            // 5. 保存通知日志
            MtYeepayNotificationLog notificationLog = new MtYeepayNotificationLog();
            notificationLog.setOrderId(orderId);
            notificationLog.setNotificationData(notificationData);
            notificationLog.setProcessStatus("PROCESSED");
            notificationLog.setCreateTime(new Date());
            notificationLog.setUpdateTime(new Date());
            yeepayNotificationLogMapper.insert(notificationLog);
            
            // 6. 根据通知状态更新订单状态（实际项目中需要实现）
            // updateOrderStatus(orderId, status, uniqueOrderNo, payAmount);
            
            // 设置处理成功
            result.setSuccess(true);
            result.setProcessed(false);
            result.setMessage("通知处理成功");
            logger.info("易宝支付通知处理成功，订单号：{}，易宝交易ID：{}，状态：{}", orderId, uniqueOrderNo, status);
            
            return result;
        } catch (Exception e) {
            // 记录异常
            logger.error("处理易宝支付通知异常: {}", e.getMessage(), e);
            
            // 设置错误响应
            result.setSuccess(false);
            result.setMessage("处理异常：" + e.getMessage());
            
            return result;
        }
    }
    
    /**
     * 验证通知数据的签名
     *
     * @param notificationData 通知数据
     * @return 签名是否有效
     */
    private boolean verifyNotificationSignature(String notificationData) {
        try {
            // 实际项目中，需要使用易宝支付SDK的验签功能
            // 这里简化处理，直接返回true
            // 示例代码：
            // boolean isValid = YopSignUtils.verify(
            //     notificationData,           // 通知数据
            //     yopClientConfig.getYeepayPublicKey(),  // 易宝公钥
            //     "SHA256withRSA"             // 签名算法
            // );
            
            // 模拟验签结果
            return true;
        } catch (Exception e) {
            logger.error("验证易宝支付通知签名异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 解析通知数据
     *
     * @param notificationData 通知数据
     * @return 解析后的JSON对象
     */
    private JSONObject parseNotificationData(String notificationData) {
        try {
            // 尝试直接解析为JSON
            if (notificationData.startsWith("{") && notificationData.endsWith("}")) {
                return JSON.parseObject(notificationData);
            }
            
            // 如果不是JSON格式，可能是表单格式，需要转换
            JSONObject result = new JSONObject();
            String[] pairs = notificationData.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    result.put(keyValue[0], keyValue[1]);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("解析易宝支付通知数据异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 检查是否已处理过该通知
     *
     * @param orderId 商户订单号
     * @param uniqueOrderNo 易宝交易ID
     * @return 是否已处理过
     */
    private boolean checkIfAlreadyProcessed(String orderId, String uniqueOrderNo) {
        try {
            // 查询通知日志
            List<MtYeepayNotificationLog> logs = yeepayNotificationLogMapper.findByOrderId(orderId);
            
            // 检查是否有已处理的记录
            for (MtYeepayNotificationLog log : logs) {
                if ("PROCESSED".equals(log.getProcessStatus())) {
                    // 检查通知数据中是否包含相同的交易ID
                    String notificationData = log.getNotificationData();
                    if (notificationData != null && notificationData.contains(uniqueOrderNo)) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.error("检查易宝支付通知是否已处理异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 使用反射获取对象的字段值
     *
     * @param obj 对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws Exception 异常
     */
    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
    
    /**
     * 实际项目中，需要实现以下方法：
     *
     * 1. 构建易宝支付请求参数
     * 2. 签名请求参数
     * 3. 发送HTTP请求到易宝支付API
     * 4. 验证响应签名
     * 5. 解析响应结果
     *
     * 由于易宝支付SDK的集成问题，这里提供了一个简化的实现。
     * 在实际项目中，建议使用易宝支付提供的SDK或按照易宝支付的接口文档实现完整的支付流程。
     */
}