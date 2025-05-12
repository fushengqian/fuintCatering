package com.fuint.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 易宝支付配置类
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Configuration
public class YopConfiguration {

    @Value("${yeepay.appKey:}")
    private String appKey;

    @Value("${yeepay.merchantNo:}")
    private String merchantNo;

    @Value("${yeepay.parentMerchantNo:}")
    private String parentMerchantNo;

    @Value("${yeepay.privateKey:}")
    private String privateKey;

    @Value("${yeepay.yeepayPublicKey:}")
    private String yeepayPublicKey;

    @Value("${yeepay.apiEndpoint:}")
    private String apiEndpoint;

    /**
     * 创建YopClientConfig Bean
     *
     * @return YopClientConfig
     */
    @Bean
    public YopClientConfig yopClientConfig() {
        return new YopClientConfig(
                appKey,
                merchantNo,
                parentMerchantNo,
                privateKey,
                yeepayPublicKey,
                apiEndpoint
        );
    }
} 