package com.fuint.module.backendApi.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * 易宝支付请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@Getter
@Setter
public class YeepayPaymentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value="订单ID", name="orderId")
    private Integer orderId;

    @ApiModelProperty(value="支付方式，如USER_SCAN、MINI_PROGRAM、WECHAT_OFFIACCOUNT、ALIPAY_LIFE、JS_PAY、SDK_PAY、H5_PAY", name="payWay")
    private String payWay;

    @ApiModelProperty(value="渠道类型，如WECHAT、ALIPAY、UNIONPAY、DCEP", name="channel")
    private String channel;

    @ApiModelProperty(value="用户ID", name="userId")
    private Integer userId;

    @ApiModelProperty(value="商户ID", name="merchantId")
    private Integer merchantId;

    @ApiModelProperty(value="店铺ID", name="storeId")
    private Integer storeId;

    @ApiModelProperty(value="回调地址", name="callbackUrl")
    private String callbackUrl;

    @ApiModelProperty(value="额外参数", name="extraParams")
    private String extraParams;
}