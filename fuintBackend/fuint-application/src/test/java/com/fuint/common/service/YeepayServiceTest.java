package com.fuint.common.service;

import com.fuint.application.config.YopClientConfig;
import com.fuint.common.dto.YeepayOrderQueryResponse;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.common.service.impl.YeepayServiceImpl;
import com.fuint.repository.model.MtOrder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * YeepayService单元测试类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@RunWith(MockitoJUnitRunner.class)
public class YeepayServiceTest {

    @Mock
    private YopClientConfig yopClientConfig;

    @InjectMocks
    private YeepayServiceImpl yeepayService;

    private MtOrder mockOrder;

    @Before
    public void setUp() {
        // 配置YopClientConfig的模拟行为
        when(yopClientConfig.getAppKey()).thenReturn("OPR:10086032562");
        when(yopClientConfig.getMerchantNo()).thenReturn("10086039518");
        when(yopClientConfig.getParentMerchantNo()).thenReturn("10086032562");
        when(yopClientConfig.getApiEndpoint()).thenReturn("https://sandbox.yeepay.com/yop-center");

        // 创建模拟订单
        mockOrder = new MtOrder();
        mockOrder.setId(1);
        
        // 使用反射设置私有字段
        try {
            java.lang.reflect.Field orderSnField = MtOrder.class.getDeclaredField("orderSn");
            orderSnField.setAccessible(true);
            orderSnField.set(mockOrder, "TEST_ORDER_123456");
            
            java.lang.reflect.Field payAmountField = MtOrder.class.getDeclaredField("payAmount");
            payAmountField.setAccessible(true);
            payAmountField.set(mockOrder, new BigDecimal("100.00"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInitiatePayment_Success() throws Exception {
        // 调用被测试的方法
        YeepayPaymentResponse response = yeepayService.initiatePayment(mockOrder);

        // 验证结果
        assertNotNull(response);
        assertEquals("SUCCESS", response.getCode());
        assertEquals("支付请求成功", response.getMessage());
        assertNotNull(response.getPrePayTn());
        assertNotNull(response.getUniqueOrderNo());
        assertEquals("TEST_ORDER_123456", response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getOrderAmount());
        assertEquals("PROCESSING", response.getStatus());
        assertNotNull(response.getPayLink());
    }

    @Test
    public void testQueryOrder_Success() throws Exception {
        // 调用被测试的方法
        YeepayOrderQueryResponse response = yeepayService.queryOrder("TEST_ORDER_123456");

        // 验证结果
        assertNotNull(response);
        assertEquals("SUCCESS", response.getCode());
        assertEquals("查询成功", response.getMessage());
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getUniqueOrderNo());
        assertEquals("TEST_ORDER_123456", response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getOrderAmount());
        assertNotNull(response.getPayTime());
        assertEquals("SCAN_CODE", response.getPayWay());
        assertEquals("WECHAT", response.getPayChannel());
    }
}