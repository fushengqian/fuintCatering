package com.fuint.application.service.payment;

import com.fuint.application.config.YopClientConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 易宝支付服务
 * 
 * 注意：由于类加载问题，这个类不直接初始化YopClient
 * 而是提供配置信息，供实际业务代码中使用
 */
@Slf4j
public class YopPaymentService {

    private YopClientConfig yopClientConfig;

    /**
     * 初始化配置
     */
    public void init() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                log.error("无法找到application.properties文件");
            } else {
                props.load(input);
            }
        } catch (IOException ex) {
            log.error("加载application.properties文件出错: " + ex.getMessage());
        }

        String appKey = props.getProperty("yeepay.appKey");
        String merchantNo = props.getProperty("yeepay.merchantNo");
        String parentMerchantNo = props.getProperty("yeepay.parentMerchantNo");
        String privateKey = props.getProperty("yeepay.privateKey");
        String yeepayPublicKey = props.getProperty("yeepay.yeepayPublicKey");
        String apiEndpoint = props.getProperty("yeepay.apiEndpoint");

        if (appKey == null || privateKey == null || apiEndpoint == null) {
            log.error("易宝支付关键配置缺失，无法初始化YopClient");
            return;
        }
        
        this.yopClientConfig = new YopClientConfig(
            appKey,
            merchantNo,
            parentMerchantNo,
            privateKey,
            yeepayPublicKey,
            apiEndpoint
        );
        
        log.info("YopClientConfig初始化成功");
        this.yopClientConfig.logConfigValues();
    }

    /**
     * 测试配置初始化
     *
     * @return 测试结果
     */
    public String testYopConfig() {
        if (this.yopClientConfig == null) {
            init(); // 如果未初始化，先初始化
        }
        
        if (this.yopClientConfig == null) {
            return "YopClientConfig初始化失败，请检查配置";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("易宝支付配置信息：\n");
        result.append("AppKey: ").append(this.yopClientConfig.getAppKey()).append("\n");
        result.append("商户编号: ").append(this.yopClientConfig.getMerchantNo()).append("\n");
        result.append("父商户编号: ").append(this.yopClientConfig.getParentMerchantNo()).append("\n");
        result.append("API端点: ").append(this.yopClientConfig.getApiEndpoint()).append("\n");
        
        return result.toString();
    }
    
    /**
     * 获取YopClientConfig
     *
     * @return YopClientConfig实例
     */
    public YopClientConfig getYopClientConfig() {
        if (this.yopClientConfig == null) {
            init(); // 如果未初始化，先初始化
        }
        return this.yopClientConfig;
    }
    
    /**
     * 使用示例：如何在业务代码中使用YopClient
     * 
     * 注意：这只是示例代码，实际使用时需要根据易宝支付SDK文档进行调整
     */
    public String getYopClientUsageExample() {
        return "// 使用YopClientConfig初始化YopClient示例\n" +
               "YopClientConfig config = yopPaymentService.getYopClientConfig();\n" +
               "// 使用YOP SDK初始化YopClient\n" +
               "// YopClient yopClient = YopClientBuilder.builder()\n" +
               "//     .withCredentials(new YopPKICredentials(config.getAppKey(), \n" +
               "//         new PKICredentialsItem(config.getPrivateKey(), CertTypeEnum.RSA2048)))\n" +
               "//     .withEndpoint(config.getApiEndpoint())\n" +
               "//     .build();\n" +
               "// \n" +
               "// // 创建请求\n" +
               "// YopRequest request = new YopRequest(\"/rest/v1.0/test\", \"POST\");\n" +
               "// // 发送请求\n" +
               "// YopResponse response = yopClient.request(request);";
    }
}