package com.fuint.common.param;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单结算请求参数
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class SettlementParam implements Serializable {

    @Schema(description="购物车Id，逗号分隔")
    @JsonDeserialize(using = StringOrCsvDeserializer.class)
    private String cartIds;

    @Schema(description="购买对象，购买储值卡、升级会员等级必填")
    private String targetId;

    @Schema(description="购买储值卡数量")
    private String selectNum;

    @Schema(description="结算备注")
    private String remark;

    @Schema(description="订单类型，payment：付款订单、goods：商品订单、recharge：充值订单、prestore：储值卡订单、member：会员升级订单")
    private String type;

    @Schema(description="支付金额，付款类订单必填")
    private String payAmount;

    @Schema(description="使用积分数量")
    private Integer usePoint;

    @Schema(description="使用卡券ID")
    private Integer couponId;

    @Schema(description="使用卡券ID列表，逗号分隔（储值卡叠加使用）")
    private String couponIds;

    @Schema(description="支付类型，CASH：现金支付，JSAPI：微信支付，MICROPAY：微信扫码支付，BALANCE：余额支付，ALISCAN：支付宝扫码")
    private String payType;

    @Schema(description="PC端扫码支付的二维码")
    private String authCode;

    @Schema(description="会员ID（代客下单用到）")
    private Integer userId;

    @Schema(description="会员手机号")
    private String mobile;

    @Schema(description="实付金额（收银台用到）")
    private String cashierPayAmount;

    @Schema(description="优惠金额（收银台用到）")
    private String cashierDiscountAmount;

    @Schema(description="商品ID")
    private Integer goodsId;

    @Schema(description="商品skuID")
    private Integer skuId;

    @Schema(description="购买数量")
    private Double buyNum;

    @Schema(description="订单模式，配送（express）或自提（oneself）")
    private String orderMode;

    @Schema(description="订单ID")
    private Integer orderId;

    @Schema(description="是否微信客户端")
    private String isWechat;

    @Schema(description="员工ID")
    private Integer staffId;

    @Schema(description="桌台")
    private Integer tableId;

    @Schema(description="就餐人数")
    private Integer peopleNum;

    /**
     * 兼容逗号分隔字符串（"1,2,3"）和数组（[1,2,3]）两种格式，统一转为逗号分隔字符串
     */
    public static class StringOrCsvDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.START_ARRAY) {
                List<String> ids = new ArrayList<>();
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    String value = p.getValueAsString();
                    if (value != null && value.length() > 0) {
                        ids.add(value);
                    }
                }
                return String.join(",", ids);
            }
            return p.getValueAsString();
        }
    }

}
