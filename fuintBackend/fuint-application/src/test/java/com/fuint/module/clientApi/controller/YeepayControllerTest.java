package com.fuint.module.clientApi.controller;

import com.alibaba.fastjson.JSONObject;
import com.fuint.common.dto.OrderDto;
import com.fuint.common.dto.UserInfo;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.YeepayService;
import com.fuint.framework.web.ResponseObject;
import com.fuint.module.clientApi.controller.YeepayController;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtYeepayNotificationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * YeepayController集成测试
 */
@ExtendWith(MockitoExtension.class)
public class YeepayControllerTest {

    private MockMvc mockMvc;

    @Mock
    private YeepayService yeepayService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private YeepayController yeepayController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(yeepayController).build();
    }

    @Test
    public void testInitiatePayment_Success() throws Exception {
        // 准备测试数据
        String orderId = "123456";
        String payWay = "USER_SCAN";
        String channel = "WECHAT";
        
        // 模拟订单
        MtOrder order = new MtOrder();
        order.setId(Integer.parseInt(orderId));
        order.setOrderSn("ORDER_" + orderId);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus("CREATED");
        
        // 模拟订单服务返回
        when(orderService.getOrderById(anyString())).thenReturn(order);
        
        // 模拟易宝支付服务返回
        Map<String, Object> paymentResult = new HashMap<>();
        paymentResult.put("prePayTn", "https://qr.yeepay.com/123456789");
        paymentResult.put("uniqueOrderNo", "YP" + orderId);
        when(yeepayService.initiatePayment(any(MtOrder.class), eq(payWay), eq(channel))).thenReturn(paymentResult);
        
        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1);
        request.setAttribute("userInfo", userInfo);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("orderId", orderId);
        requestBody.put("payWay", payWay);
        requestBody.put("channel", channel);
        
        ResponseObject response = yeepayController.initiatePayment(request, requestBody);
        
        // 验证结果
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertNotNull(response.getData());
        
        // 验证服务调用
        verify(orderService, times(1)).getOrderById(eq(orderId));
        verify(yeepayService, times(1)).initiatePayment(any(MtOrder.class), eq(payWay), eq(channel));
    }

    @Test
    public void testInitiatePayment_OrderNotFound() throws Exception {
        // 准备测试数据
        String orderId = "999999";
        String payWay = "USER_SCAN";
        String channel = "WECHAT";
        
        // 模拟订单服务返回空
        when(orderService.getOrderById(anyString())).thenReturn(null);
        
        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1);
        request.setAttribute("userInfo", userInfo);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("orderId", orderId);
        requestBody.put("payWay", payWay);
        requestBody.put("channel", channel);
        
        ResponseObject response = yeepayController.initiatePayment(request, requestBody);
        
        // 验证结果
        assertNotNull(response);
        assertNotEquals("200", response.getCode());
        
        // 验证服务调用
        verify(orderService, times(1)).getOrderById(eq(orderId));
        verify(yeepayService, never()).initiatePayment(any(MtOrder.class), anyString(), anyString());
    }

    @Test
    public void testPaymentNotify_ValidSignature() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"123456\",\"status\":\"SUCCESS\",\"amount\":\"100.00\"}";
        
        // 模拟易宝支付服务处理通知
        MtYeepayNotificationLog log = new MtYeepayNotificationLog();
        log.setId(1);
        log.setOrderId("123456");
        log.setNotificationData(notificationData);
        log.setProcessStatus("PROCESSED");
        log.setCreateTime(new Date());
        
        when(yeepayService.processNotification(anyString())).thenReturn(log);
        
        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(notificationData.getBytes());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String result = yeepayController.paymentNotify(request, response);
        
        // 验证结果
        assertEquals("success", result);
        
        // 验证服务调用
        verify(yeepayService, times(1)).processNotification(eq(notificationData));
    }

    @Test
    public void testPaymentNotify_InvalidSignature() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"123456\",\"status\":\"SUCCESS\",\"amount\":\"100.00\"}";
        
        // 模拟易宝支付服务处理通知抛出异常
        when(yeepayService.processNotification(anyString())).thenThrow(new RuntimeException("Invalid signature"));
        
        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(notificationData.getBytes());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        String result = yeepayController.paymentNotify(request, response);
        
        // 验证结果
        assertEquals("fail", result);
        
        // 验证服务调用
        verify(yeepayService, times(1)).processNotification(eq(notificationData));
    }

    @Test
    public void testQueryOrder_Success() throws Exception {
        // 准备测试数据
        String orderId = "123456";
        
        // 模拟订单
        MtOrder order = new MtOrder();
        order.setId(Integer.parseInt(orderId));
        order.setOrderSn("ORDER_" + orderId);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus("PAID");
        order.setYeepayStatus("SUCCESS");
        order.setYeepayTransactionId("YP" + orderId);
        
        // 模拟订单服务返回
        when(orderService.getOrderById(anyString())).thenReturn(order);
        
        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1);
        request.setAttribute("userInfo", userInfo);
        
        ResponseObject response = yeepayController.queryOrder(request, orderId);
        
        // 验证结果
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertNotNull(response.getData());
        
        // 验证服务调用
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }

    @Test
    public void testQueryOrder_OrderNotFound() throws Exception {
        // 准备测试数据
        String orderId = "999999";
        
        // 模拟订单服务返回空
        when(orderService.getOrderById(anyString())).thenReturn(null);
        
        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1);
        request.setAttribute("userInfo", userInfo);
        
        ResponseObject response = yeepayController.queryOrder(request, orderId);
        
        // 验证结果
        assertNotNull(response);
        assertNotEquals("200", response.getCode());
        
        // 验证服务调用
        verify(orderService, times(1)).getOrderById(eq(orderId));
    }
}