package com.fuint.application.service.payment;

import com.fuint.application.config.YopClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * YopPaymentService单元测试
 */
@ExtendWith(MockitoExtension.class)
public class YeepayServiceTest {

    @InjectMocks
    @Spy
    private YopPaymentService yopPaymentService;

    @Mock
    private YopClientConfig mockYopClientConfig;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 模拟配置文件内容
        String propertiesContent = 
                "yeepay.appKey=OPR:10086032562\n" +
                "yeepay.merchantNo=10086039518\n" +
                "yeepay.parentMerchantNo=10086032562\n" +
                "yeepay.privateKey=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDQz7mq5/rnJGl...\n" +
                "yeepay.yeepayPublicKey=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XB0...\n" +
                "yeepay.apiEndpoint=https://sandbox.yeepay.com/yop-center";
        
        InputStream inputStream = new ByteArrayInputStream(propertiesContent.getBytes(StandardCharsets.UTF_8));
        
        // 模拟类加载器返回我们的模拟配置
        doReturn(inputStream).when(yopPaymentService).getClass();
        doReturn(inputStream).when(yopPaymentService).getClass().getClassLoader().getResourceAsStream(anyString());
        
        // 手动设置mockYopClientConfig
        YopClientConfig realConfig = new YopClientConfig(
                "OPR:10086032562",
                "10086039518",
                "10086032562",
                "MIIEvQIBADANBgkqhkiG9...",
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XB0...",
                "https://sandbox.yeepay.com/yop-center"
        );
        ReflectionTestUtils.setField(yopPaymentService, "yopClientConfig", realConfig);
    }

    @Test
    public void testInit() {
        // 执行初始化方法
        yopPaymentService.init();
        
        // 验证yopClientConfig已被正确初始化
        YopClientConfig config = yopPaymentService.getYopClientConfig();
        assertNotNull(config, "YopClientConfig不应为空");
        assertEquals("OPR:10086032562", config.getAppKey(), "AppKey应正确设置");
        assertEquals("10086039518", config.getMerchantNo(), "MerchantNo应正确设置");
        assertEquals("10086032562", config.getParentMerchantNo(), "ParentMerchantNo应正确设置");
        assertNotNull(config.getPrivateKey(), "PrivateKey不应为空");
        assertNotNull(config.getYeepayPublicKey(), "YeepayPublicKey不应为空");
        assertEquals("https://sandbox.yeepay.com/yop-center", config.getApiEndpoint(), "ApiEndpoint应正确设置");
    }

    @Test
    public void testGetYopClientConfig() {
        // 设置一个模拟的YopClientConfig
        YopClientConfig mockConfig = new YopClientConfig(
                "testAppKey",
                "testMerchantNo",
                "testParentMerchantNo",
                "testPrivateKey",
                "testYeepayPublicKey",
                "testApiEndpoint"
        );
        ReflectionTestUtils.setField(yopPaymentService, "yopClientConfig", mockConfig);
        
        // 调用getYopClientConfig方法
        YopClientConfig result = yopPaymentService.getYopClientConfig();
        
        // 验证结果
        assertNotNull(result, "返回的YopClientConfig不应为空");
        assertEquals("testAppKey", result.getAppKey());
        assertEquals("testMerchantNo", result.getMerchantNo());
        assertEquals("testParentMerchantNo", result.getParentMerchantNo());
        assertEquals("testPrivateKey", result.getPrivateKey());
        assertEquals("testYeepayPublicKey", result.getYeepayPublicKey());
        assertEquals("testApiEndpoint", result.getApiEndpoint());
    }

    @Test
    public void testTestYopConfig() {
        // 设置一个模拟的YopClientConfig
        YopClientConfig mockConfig = new YopClientConfig(
                "testAppKey",
                "testMerchantNo",
                "testParentMerchantNo",
                "testPrivateKey",
                "testYeepayPublicKey",
                "testApiEndpoint"
        );
        ReflectionTestUtils.setField(yopPaymentService, "yopClientConfig", mockConfig);
        
        // 调用testYopConfig方法
        String result = yopPaymentService.testYopConfig();
        
        // 验证结果
        assertNotNull(result, "返回的结果不应为空");
        assertTrue(result.contains("testAppKey"), "结果应包含AppKey");
        assertTrue(result.contains("testMerchantNo"), "结果应包含MerchantNo");
        assertTrue(result.contains("testParentMerchantNo"), "结果应包含ParentMerchantNo");
        assertTrue(result.contains("testApiEndpoint"), "结果应包含ApiEndpoint");
    }

    @Test
    public void testGetYopClientUsageExample() {
        // 调用getYopClientUsageExample方法
        String result = yopPaymentService.getYopClientUsageExample();
        
        // 验证结果
        assertNotNull(result, "返回的结果不应为空");
        assertTrue(result.contains("YopClientConfig"), "结果应包含YopClientConfig");
        assertTrue(result.contains("YopClient"), "结果应包含YopClient");
    }
}