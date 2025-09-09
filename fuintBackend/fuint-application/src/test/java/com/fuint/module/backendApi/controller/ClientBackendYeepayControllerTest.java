package com.fuint.module.backendApi.controller;

import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.YeepayService;
import com.fuint.common.util.YeepayTestUtils;
import com.fuint.framework.web.ResponseObject;
import com.fuint.module.backendApi.request.YeepayPaymentRequest;
import com.fuint.repository.model.MtOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * YeepayController单元测试
 */
@ExtendWith(MockitoExtension.class)
public class ClientBackendYeepayControllerTest {

    @InjectMocks
    private BackendYeepayController backendYeepayController;

    @Mock
    private YeepayService yeepayService;

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(backendYeepayController).build();
    }

    @Test
    public void testInitiatePayment_Success() throws Exception {
        // 准备测试数据
        String orderId = "123456";
        String payWay = "SCAN_CODE";
        String channel = "WECHAT";

        // 创建请求对象
        YeepayPaymentRequest requestBody = new YeepayPaymentRequest();
        ReflectionTestUtils.setField(requestBody, "orderId", Integer.parseInt(orderId));
        ReflectionTestUtils.setField(requestBody, "payWay", payWay);
        ReflectionTestUtils.setField(requestBody, "channel", channel);

        // 模拟订单服务返回订单信息
        UserOrderDto orderDto = new UserOrderDto();
        ReflectionTestUtils.setField(orderDto, "id", Integer.parseInt(orderId));
        ReflectionTestUtils.setField(orderDto, "orderSn", "ORDER_" + orderId);
        ReflectionTestUtils.setField(orderDto, "amount", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(orderDto, "payAmount", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(orderDto, "status", "CREATED");
        when(orderService.getOrderById(eq(Integer.parseInt(orderId)))).thenReturn(orderDto);

        // 模拟易宝支付服务返回支付结果
        YeepayPaymentResponse paymentResult = YeepayTestUtils.createMockPaymentResponse("ORDER_" + orderId, new BigDecimal("100.00"));
        when(yeepayService.initiatePayment(any(MtOrder.class))).thenReturn(paymentResult);

        // 执行测试
        ResponseObject response = backendYeepayController.initiatePayment(requestBody);

        // 验证结果
        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertEquals("支付请求成功", response.getMessage());
        assertNotNull(response.getData());

        // 验证服务调用
        verify(orderService, times(1)).getOrderById(eq(Integer.parseInt(orderId)));
        verify(yeepayService, times(1)).initiatePayment(any(MtOrder.class));
    }

    @Test
    public void testInitiatePayment_OrderNotFound() throws Exception {
        // 准备测试数据
        String orderId = "123456";
        String payWay = "SCAN_CODE";
        String channel = "WECHAT";

        // 创建请求对象
        YeepayPaymentRequest requestBody = new YeepayPaymentRequest();
        ReflectionTestUtils.setField(requestBody, "orderId", Integer.parseInt(orderId));
        ReflectionTestUtils.setField(requestBody, "payWay", payWay);
        ReflectionTestUtils.setField(requestBody, "channel", channel);

        // 模拟订单服务返回null（订单不存在）
        when(orderService.getOrderById(eq(Integer.parseInt(orderId)))).thenReturn(null);

        // 执行测试
        ResponseObject response = backendYeepayController.initiatePayment(requestBody);

        // 验证结果
        assertNotNull(response);
        assertEquals(201, response.getCode());
        assertEquals("订单不存在", response.getMessage());

        // 验证服务调用
        verify(orderService, times(1)).getOrderById(eq(Integer.parseInt(orderId)));
        verify(yeepayService, never()).initiatePayment(any(MtOrder.class));
    }

    @Test
    public void testPaymentNotify_Success() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"ORDER_123456\",\"uniqueOrderNo\":\"YOP_ORDER_123456\",\"status\":\"SUCCESS\",\"payAmount\":100.00,\"paySuccessDate\":\"2025-05-08 02:00:00\"}";

        // 创建请求和响应对象
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // 模拟请求体内容
        request.setContent(notificationData.getBytes());
        
        // 模拟YeepayService.processNotification方法
        YeepayNotificationResult notificationResult = new YeepayNotificationResult();
        ReflectionTestUtils.setField(notificationResult, "success", true);
        ReflectionTestUtils.setField(notificationResult, "processed", false);
        ReflectionTestUtils.setField(notificationResult, "message", "通知处理成功");
        ReflectionTestUtils.setField(notificationResult, "orderId", "ORDER_123456");
        ReflectionTestUtils.setField(notificationResult, "uniqueOrderNo", "YOP_ORDER_123456");
        ReflectionTestUtils.setField(notificationResult, "status", "SUCCESS");
        ReflectionTestUtils.setField(notificationResult, "payAmount", 100.00);
        ReflectionTestUtils.setField(notificationResult, "paySuccessDate", "2025-05-08 02:00:00");
        
        when(yeepayService.processNotification(anyString())).thenReturn(notificationResult);
        
        // 执行测试
        String result = backendYeepayController.paymentNotify(request);
        
        // 验证结果
        assertEquals("success", result);
        
        // 验证服务调用
        verify(yeepayService, times(1)).processNotification(anyString());
    }

    @Test
    public void testPaymentNotify_AlreadyProcessed() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"ORDER_123456\",\"uniqueOrderNo\":\"YOP_ORDER_123456\",\"status\":\"SUCCESS\",\"payAmount\":100.00,\"paySuccessDate\":\"2025-05-08 02:00:00\"}";

        // 创建请求和响应对象
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // 模拟请求体内容
        request.setContent(notificationData.getBytes());
        
        // 模拟YeepayService.processNotification方法
        YeepayNotificationResult notificationResult = new YeepayNotificationResult();
        ReflectionTestUtils.setField(notificationResult, "success", true);
        ReflectionTestUtils.setField(notificationResult, "processed", true);
        ReflectionTestUtils.setField(notificationResult, "message", "通知已处理过");
        ReflectionTestUtils.setField(notificationResult, "orderId", "ORDER_123456");
        
        when(yeepayService.processNotification(anyString())).thenReturn(notificationResult);
        
        // 执行测试
        String result = backendYeepayController.paymentNotify(request);
        
        // 验证结果
        assertEquals("success", result);
        
        // 验证服务调用
        verify(yeepayService, times(1)).processNotification(anyString());
    }

    @Test
    public void testPaymentNotify_Exception() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"ORDER_123456\",\"uniqueOrderNo\":\"YOP_ORDER_123456\",\"status\":\"SUCCESS\",\"payAmount\":100.00,\"paySuccessDate\":\"2025-05-08 02:00:00\"}";

        // 创建请求和响应对象
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // 模拟请求体内容
        request.setContent(notificationData.getBytes());
        
        // 模拟YeepayService.processNotification方法抛出异常
        when(yeepayService.processNotification(anyString())).thenThrow(new RuntimeException("处理通知时发生异常"));
        
        // 执行测试
        String result = backendYeepayController.paymentNotify(request);
        
        // 验证结果
        assertEquals("success", result);
        
        // 验证服务调用
        verify(yeepayService, times(1)).processNotification(anyString());
    }
}