package com.fuint.application.config;

// This class is a Plain Old Java Object (POJO) to hold configuration.
// It does not use Spring annotations to avoid the import/resolution issues.
public class YopClientConfig {

    private String appKey;
    private String merchantNo;
    private String parentMerchantNo;
    private String privateKey;
    private String yeepayPublicKey; // This will hold the placeholder string
    private String apiEndpoint;

    public YopClientConfig(String appKey, String merchantNo, String parentMerchantNo,
                           String privateKey, String yeepayPublicKey, String apiEndpoint) {
        this.appKey = appKey;
        this.merchantNo = merchantNo;
        this.parentMerchantNo = parentMerchantNo;
        this.privateKey = privateKey;
        this.yeepayPublicKey = yeepayPublicKey;
        this.apiEndpoint = apiEndpoint;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public String getParentMerchantNo() {
        return parentMerchantNo;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getYeepayPublicKey() {
        return yeepayPublicKey;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public String logConfigValues() {
        StringBuilder sb = new StringBuilder();
        sb.append("YOP Configuration Values (from YopClientConfig POJO):\n");
        sb.append("  AppKey: ").append(appKey).append("\n");
        sb.append("  MerchantNo: ").append(merchantNo).append("\n");
        sb.append("  ParentMerchantNo: ").append(parentMerchantNo).append("\n");
        sb.append("  ApiEndpoint: ").append(apiEndpoint).append("\n");
        // Not logging privateKey or yeepayPublicKey for security in production logs,
        // but yeepayPublicKey is a placeholder here.
        sb.append("  YeepayPublicKey (Placeholder): ").append(yeepayPublicKey).append("\n");
        System.out.println(sb.toString()); // Print to console for immediate feedback
        return sb.toString();
    }
}