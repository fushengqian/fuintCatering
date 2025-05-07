package com.fuint.application.config;

import com.yeepay.yop.sdk.YopClient;
import com.yeepay.yop.sdk.YopClientConfiguration;
import com.yeepay.yop.sdk.security.Credentials;
import com.yeepay.yop.sdk.security.CredentialsProvider;
import com.yeepay.yop.sdk.security.SimpleCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YopClientConfig {

    @Value("${yeepay.appKey}")
    private String appKey;

    @Value("${yeepay.merchantNo}")
    private String merchantNo;

    @Value("${yeepay.parentMerchantNo}")
    private String parentMerchantNo;

    @Value("${yeepay.privateKey}")
    private String privateKey;

    @Value("${yeepay.yeepayPublicKey}")
    private String yeepayPublicKey;

    @Value("${yeepay.apiEndpoint}")
    private String apiEndpoint;

    @Bean
    public YopClient yopClient() {
        CredentialsProvider credentialsProvider = new SimpleCredentialsProvider(
                new Credentials(appKey, privateKey, yeepayPublicKey)
        );

        YopClientConfiguration configuration = YopClientConfiguration.builder()
                .setAppKey(appKey)
                .setServerRoot(apiEndpoint)
                .setCredentialsProvider(credentialsProvider)
                .build();

        return new YopClient(configuration);
    }
}