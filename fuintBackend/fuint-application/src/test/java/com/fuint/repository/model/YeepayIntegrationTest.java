package com.fuint.repository.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 易宝支付集成相关实体类测试
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public class YeepayIntegrationTest {

    /**
     * 测试订单实体类中的易宝支付字段
     */
    @Test
    public void testOrderYeepayFields() {
        // 创建订单对象
        MtOrder order = new MtOrder();
        
        // 设置易宝支付相关字段
        order.setYeepayTransactionId("YP12345678901234");
        order.setYeepayStatus("SUCCESS");
        order.setPaymentGateway("YEEPAY");
        
        // 验证字段值
        assertEquals("YP12345678901234", order.getYeepayTransactionId());
        assertEquals("SUCCESS", order.getYeepayStatus());
        assertEquals("YEEPAY", order.getPaymentGateway());
    }
    
    /**
     * 测试易宝支付通知日志实体类
     */
    @Test
    public void testYeepayNotificationLog() {
        // 创建易宝支付通知日志对象
        MtYeepayNotificationLog log = new MtYeepayNotificationLog();
        
        // 设置字段值
        log.setId(1);
        log.setOrderId("O2023052312345");
        log.setNotificationData("{\"code\":\"SUCCESS\",\"message\":\"支付成功\",\"transactionId\":\"YP12345678901234\"}");
        log.setProcessStatus("PROCESSED");
        log.setErrorMessage(null);
        Date now = new Date();
        log.setCreateTime(now);
        log.setUpdateTime(now);
        
        // 验证字段值
        assertEquals(1, log.getId());
        assertEquals("O2023052312345", log.getOrderId());
        assertEquals("{\"code\":\"SUCCESS\",\"message\":\"支付成功\",\"transactionId\":\"YP12345678901234\"}", log.getNotificationData());
        assertEquals("PROCESSED", log.getProcessStatus());
        assertNull(log.getErrorMessage());
        assertEquals(now, log.getCreateTime());
        assertEquals(now, log.getUpdateTime());
    }
}