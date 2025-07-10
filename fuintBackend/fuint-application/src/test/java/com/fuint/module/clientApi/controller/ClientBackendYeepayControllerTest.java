package com.fuint.module.clientApi.controller;

import com.alibaba.fastjson.JSONObject;
import com.fuint.common.dto.UserInfo;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.YeepayService;
import com.fuint.framework.web.ResponseObject;
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * YeepayController集成测试
 */
@ExtendWith(MockitoExtension.class)
public class ClientBackendYeepayControllerTest {

    @Mock
    private YeepayService yeepayService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ClientYeepayController clientYeepayController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInitiatePayment_Success() throws Exception {
        // 准备测试数据
        String orderId = "123456";
        String payWay = "USER_SCAN";
        String channel = "WECHAT";

        // 模拟订单
        UserOrderDto orderDto = new UserOrderDto();
        ReflectionTestUtils.setField(orderDto, "id", 123456);
        ReflectionTestUtils.setField(orderDto, "orderSn", "ORDER_123456");
        ReflectionTestUtils.setField(orderDto, "amount", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(orderDto, "status", "CREATED");
        ReflectionTestUtils.setField(orderDto, "userId", 1);

        // 模拟订单服务返回
        when(orderService.getOrderById(123456)).thenReturn(orderDto);

        // 模拟易宝支付服务返回
        YeepayPaymentResponse paymentResult = new YeepayPaymentResponse();
        ReflectionTestUtils.setField(paymentResult, "code", "SUCCESS");
        ReflectionTestUtils.setField(paymentResult, "prePayTn", "https://qr.yeepay.com/123456789");
        ReflectionTestUtils.setField(paymentResult, "uniqueOrderNo", "YP123456");
        when(yeepayService.initiatePayment(any(MtOrder.class))).thenReturn(paymentResult);

        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        ReflectionTestUtils.setField(userInfo, "id", 1);
        request.setAttribute("userInfo", userInfo);

        JSONObject requestBody = new JSONObject();
        requestBody.put("orderId", orderId);
        requestBody.put("payWay", payWay);
        requestBody.put("channel", channel);

        ResponseObject response = clientYeepayController.initiatePayment(request, requestBody);

        // 验证结果
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertNotNull(response.getData());

        // 验证服务调用
        verify(orderService, times(1)).getOrderById(123456);
        verify(yeepayService, times(1)).initiatePayment(any(MtOrder.class));
    }

    @Test
    public void testInitiatePayment_OrderNotFound() throws Exception {
        // 准备测试数据
        String orderId = "999999";
        String payWay = "USER_SCAN";
        String channel = "WECHAT";

        // 模拟订单服务返回空
        when(orderService.getOrderById(999999)).thenReturn(null);

        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        ReflectionTestUtils.setField(userInfo, "id", 1);
        request.setAttribute("userInfo", userInfo);

        JSONObject requestBody = new JSONObject();
        requestBody.put("orderId", orderId);
        requestBody.put("payWay", payWay);
        requestBody.put("channel", channel);

        ResponseObject response = clientYeepayController.initiatePayment(request, requestBody);

        // 验证结果
        assertNotNull(response);
        assertNotEquals("200", response.getCode());

        // 验证服务调用
        verify(orderService, times(1)).getOrderById(999999);
        verify(yeepayService, never()).initiatePayment(any(MtOrder.class));
    }

    @Test
    public void testPaymentNotify_ValidSignature() throws Exception {
        // 准备测试数据
        String notificationData = "{\"orderId\":\"123456\",\"status\":\"SUCCESS\",\"amount\":\"100.00\"}";

        // 模拟易宝支付服务处理通知
        YeepayNotificationResult result = new YeepayNotificationResult();
        ReflectionTestUtils.setField(result, "success", true);
        ReflectionTestUtils.setField(result, "processed", false);
        ReflectionTestUtils.setField(result, "orderId", "123456");
        ReflectionTestUtils.setField(result, "status", "SUCCESS");

        when(yeepayService.processNotification(anyString())).thenReturn(result);

        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(notificationData.getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        String responseText = clientYeepayController.paymentNotify(request, response);

        // 验证结果
        assertEquals("success", responseText);

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

        String responseText = clientYeepayController.paymentNotify(request, response);

        // 验证结果
        assertEquals("fail", responseText);

        // 验证服务调用
        verify(yeepayService, times(1)).processNotification(eq(notificationData));
    }

    @Test
    public void testQueryOrder_Success() throws Exception {
        // 准备测试数据
        String orderId = "123456";

        // 模拟订单
        UserOrderDto orderDto = new UserOrderDto();
        ReflectionTestUtils.setField(orderDto, "id", 123456);
        ReflectionTestUtils.setField(orderDto, "orderSn", "ORDER_123456");
        ReflectionTestUtils.setField(orderDto, "amount", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(orderDto, "status", "PAID");
        ReflectionTestUtils.setField(orderDto, "userId", 1);

        // 模拟订单服务返回
        when(orderService.getOrderById(123456)).thenReturn(orderDto);

        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        ReflectionTestUtils.setField(userInfo, "id", 1);
        request.setAttribute("userInfo", userInfo);

        ResponseObject response = clientYeepayController.queryOrder(request, orderId);

        // 验证结果
        assertNotNull(response);
        assertEquals("200", response.getCode());
        assertNotNull(response.getData());

        // 验证服务调用
        verify(orderService, times(1)).getOrderById(123456);
    }

    @Test
    public void testQueryOrder_OrderNotFound() throws Exception {
        // 准备测试数据
        String orderId = "999999";

        // 模拟订单服务返回空
        when(orderService.getOrderById(999999)).thenReturn(null);

        // 执行测试
        MockHttpServletRequest request = new MockHttpServletRequest();
        UserInfo userInfo = new UserInfo();
        ReflectionTestUtils.setField(userInfo, "id", 1);
        request.setAttribute("userInfo", userInfo);

        ResponseObject response = clientYeepayController.queryOrder(request, orderId);

        // 验证结果
        assertNotNull(response);
        assertNotEquals("200", response.getCode());

        // 验证服务调用
        verify(orderService, times(1)).getOrderById(999999);
    }
}