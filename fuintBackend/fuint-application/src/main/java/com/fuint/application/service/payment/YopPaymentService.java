package com.fuint.application.service.payment;

import com.yeepay.yop.sdk.YopClient;
import com.yeepay.yop.sdk.YopClientConfiguration;
import com.yeepay.yop.sdk.security.Credentials;
import com.yeepay.yop.sdk.security.CredentialsProvider;
import com.yeepay.yop.sdk.security.SimpleCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 易宝支付服务
 */
@Service
public class YopPaymentService {

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

    /**
     * 初始化YopClient
     *
     * @return YopClient实例
     */
    public YopClient initYopClient() {
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

    /**
     * 测试YopClient初始化
     *
     * @return 测试结果
     */
    public String testYopClientInit() {
        try {
            YopClient client = initYopClient();
            if (client != null) {
                return "YopClient初始化成功，配置信息：" +
                        "\nappKey: " + appKey +
                        "\nmerchantNo: " + merchantNo +
                        "\nparentMerchantNo: " + parentMerchantNo +
                        "\napiEndpoint: " + apiEndpoint;
            } else {
                return "YopClient初始化失败";
            }
        } catch (Exception e) {
            return "YopClient初始化异常: " + e.getMessage();
        }
    }
}