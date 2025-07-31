package com.fuint.common.service;

import com.alibaba.fastjson.JSONObject;
import com.fuint.application.config.YopClientConfig;
import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayOrderQueryResponse;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.common.service.impl.YeepayServiceImpl;
import com.fuint.repository.mapper.MtYeepayNotificationLogMapper;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtYeepayNotificationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * YeepayService单元测试
 */
@ExtendWith(MockitoExtension.class)
public class YeepayServiceTest {

    @InjectMocks
    private YeepayServiceImpl yeepayService;

    @Mock
    private YopClientConfig yopClientConfig;

    @Mock
    private OrderService orderService;

    @Mock
    private MtYeepayNotificationLogMapper yeepayNotificationLogMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // 设置YopClientConfig
        when(yopClientConfig.getAppKey()).thenReturn("OPR:10086032562");
        when(yopClientConfig.getMerchantNo()).thenReturn("10086039518");
        when(yopClientConfig.getParentMerchantNo()).thenReturn("10086032562");
        when(yopClientConfig.getPrivateKey()).thenReturn("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDQz7mq5/rnJGl...");
        when(yopClientConfig.getYeepayPublicKey()).thenReturn("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XB0...");
        when(yopClientConfig.getApiEndpoint()).thenReturn("https://sandbox.yeepay.com/yop-center");
        
        // 注入依赖
        ReflectionTestUtils.setField(yeepayService, "yopClientConfig", yopClientConfig);
        ReflectionTestUtils.setField(yeepayService, "orderService", orderService);
        ReflectionTestUtils.setField(yeepayService, "yeepayNotificationLogMapper", yeepayNotificationLogMapper);
    }

    @Test
    public void testInitiatePayment_Success() throws Exception {
        // 准备测试数据
        MtOrder order = new MtOrder();
        // 使用反射设置属性，避免使用setter方法
        ReflectionTestUtils.setField(order, "id", 123456);
        ReflectionTestUtils.setField(order, "orderSn", "ORDER_123456");
        ReflectionTestUtils.setField(order, "amount", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(order, "payAmount", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(order, "status", "CREATED");
        
        // 执行测试
        YeepayPaymentResponse response = yeepayService.initiatePayment(order);
        
        // 验证结果
        assertNotNull(response);
        assertEquals("SUCCESS", response.getCode());
        assertNotNull(response.getPrePayTn());
        assertNotNull(response.getUniqueOrderNo());
        assertNotNull(response.getPayLink());
        assertEquals("ORDER_123456", response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getOrderAmount());
        assertEquals("PROCESSING", response.getStatus());
    }

    @Test
    public void testQueryOrder_Success() throws Exception {
        // 准备测试数据
        String orderId = "ORDER_123456";
        
        // 模拟orderService.updateOrderStatusFromYeepay方法
        when(orderService.updateOrderStatusFromYeepay(eq(orderId), anyString(), anyString())).thenReturn(true);
        
        // 执行测试
        YeepayOrderQueryResponse response = yeepayService.queryOrder(orderId);
        
        // 验证结果
        assertNotNull(response);
        assertEquals("SUCCESS", response.getCode());
        assertEquals("查询成功", response.getMessage());
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getUniqueOrderNo());
        assertEquals(orderId, response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getOrderAmount());
        assertNotNull(response.getPayTime());
        assertEquals("SCAN_CODE", response.getPayWay());
        assertEquals("WECHAT", response.getPayChannel());
        
        // 验证orderService.updateOrderStatusFromYeepay方法被调用
        verify(orderService, times(1)).updateOrderStatusFromYeepay(eq(orderId), eq("SUCCESS"), anyString());
    }

    @Test
    public void testProcessNotification_Success() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"ORDER_123456\",\"uniqueOrderNo\":\"YOP_ORDER_123456\",\"status\":\"SUCCESS\",\"payAmount\":100.00,\"paySuccessDate\":\"2025-05-08 02:00:00\"}";
        
        // 模拟checkIfAlreadyProcessed方法返回false（未处理过）
        List<MtYeepayNotificationLog> emptyLogs = new ArrayList<>();
        when(yeepayNotificationLogMapper.findByOrderId(anyString())).thenReturn(emptyLogs);
        
        // 模拟orderService.updateOrderStatusFromYeepay方法
        when(orderService.updateOrderStatusFromYeepay(eq("ORDER_123456"), eq("SUCCESS"), eq("YOP_ORDER_123456"))).thenReturn(true);
        
        // 执行测试
        YeepayNotificationResult result = yeepayService.processNotification(notificationData);
        
        // 验证结果
        assertNotNull(result);
        // 使用反射获取属性值，因为Lombok生成的getter可能在测试环境中不可用
        boolean success = (boolean) ReflectionTestUtils.getField(result, "success");
        boolean processed = (boolean) ReflectionTestUtils.getField(result, "processed");
        String message = (String) ReflectionTestUtils.getField(result, "message");
        String resultOrderId = (String) ReflectionTestUtils.getField(result, "orderId");
        String uniqueOrderNo = (String) ReflectionTestUtils.getField(result, "uniqueOrderNo");
        String status = (String) ReflectionTestUtils.getField(result, "status");
        Double payAmount = (Double) ReflectionTestUtils.getField(result, "payAmount");
        String paySuccessDate = (String) ReflectionTestUtils.getField(result, "paySuccessDate");
        
        assertTrue(success);
        assertFalse(processed);
        assertEquals("通知处理成功", message);
        assertEquals("ORDER_123456", resultOrderId);
        assertEquals("YOP_ORDER_123456", uniqueOrderNo);
        assertEquals("SUCCESS", status);
        assertEquals(100.00, payAmount);
        assertEquals("2025-05-08 02:00:00", paySuccessDate);
        
        // 验证yeepayNotificationLogMapper.insert方法被调用
        verify(yeepayNotificationLogMapper, times(1)).insert(any(MtYeepayNotificationLog.class));
        
        // 验证orderService.updateOrderStatusFromYeepay方法被调用
        verify(orderService, times(1)).updateOrderStatusFromYeepay(eq("ORDER_123456"), eq("SUCCESS"), eq("YOP_ORDER_123456"));
    }

    @Test
    public void testProcessNotification_AlreadyProcessed() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"ORDER_123456\",\"uniqueOrderNo\":\"YOP_ORDER_123456\",\"status\":\"SUCCESS\",\"payAmount\":100.00,\"paySuccessDate\":\"2025-05-08 02:00:00\"}";
        
        // 模拟checkIfAlreadyProcessed方法返回true（已处理过）
        List<MtYeepayNotificationLog> logs = new ArrayList<>();
        MtYeepayNotificationLog log = new MtYeepayNotificationLog();
        // Use ReflectionTestUtils to set private fields if setters are not available or for testing private state
        ReflectionTestUtils.setField(log, "orderId", "ORDER_123456");
        ReflectionTestUtils.setField(log, "notificationData", notificationData);
        ReflectionTestUtils.setField(log, "processStatus", "PROCESSED");
        logs.add(log);
        when(yeepayNotificationLogMapper.findByOrderId(eq("ORDER_123456"))).thenReturn(logs);
        
        // 执行测试
        YeepayNotificationResult result = yeepayService.processNotification(notificationData);
        
        // 验证结果
        assertNotNull(result);
        // 使用反射获取属性值
        boolean success = (boolean) ReflectionTestUtils.getField(result, "success");
        boolean processed = (boolean) ReflectionTestUtils.getField(result, "processed");
        String message = (String) ReflectionTestUtils.getField(result, "message");
        String resultOrderId = (String) ReflectionTestUtils.getField(result, "orderId");
        
        assertTrue(success);
        assertTrue(processed);
        assertEquals("通知已处理过", message);
        assertEquals("ORDER_123456", resultOrderId);
        
        // 验证yeepayNotificationLogMapper.insert方法未被调用
        verify(yeepayNotificationLogMapper, never()).insert(any(MtYeepayNotificationLog.class));
        
        // 验证orderService.updateOrderStatusFromYeepay方法未被调用
        verify(orderService, never()).updateOrderStatusFromYeepay(anyString(), anyString(), anyString());
    }
}