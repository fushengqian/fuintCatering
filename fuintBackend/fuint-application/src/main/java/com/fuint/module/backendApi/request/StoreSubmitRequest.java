package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 提交店铺信息请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class StoreSubmitRequest implements Serializable {

    @Schema(description="店铺ID")
    private Integer id;

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺名称")
    private String name;

    @Schema(description="联系人")
    private String contact;

    @Schema(description="联系手机号")
    private String phone;

    @Schema(description="备注信息")
    private String description;

    @Schema(description="是否默认店铺")
    private String isDefault;

    @Schema(description="店铺地址")
    private String address;

    @Schema(description="营业时间")
    private String hours;

    @Schema(description="维度")
    private String latitude;

    @Schema(description="经度")
    private String longitude;

    @Schema(description="微信商户号")
    private String wxMchId;

    @Schema(description="支付秘钥apiV2")
    private String wxApiV2;

    @Schema(description="微信支付证书")
    private String wxCertPath;

    @Schema(description="支付宝appId")
    private String alipayAppId;

    @Schema(description="支付宝私钥")
    private String alipayPrivateKey;

    @Schema(description="支付宝公钥")
    private String alipayPublicKey;

    @Schema(description="店铺LOGO")
    private String logo;

    @Schema(description="营业执照")
    private String license;

    @Schema(description="统一社会信用码")
    private String creditCode;

    @Schema(description="银行名称")
    private String bankName;

    @Schema(description="银行户名")
    private String bankCardName;

    @Schema(description="银行卡号")
    private String bankCardNo;

    @Schema(description="状态")
    private String status;

}
