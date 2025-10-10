package com.fuint.common.bean.shoppingOrders;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 订单，需要上传物流信息的订单Bean
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderKeyBean implements Serializable {

  private static final long serialVersionUID = 1486092394664728388L;

  /**
   * 必填
   * 订单单号类型，用于确认需要上传详情的订单。枚举值1，使用下单商户号和商户侧单号；枚举值2，使用微信支付单号。
   */
  @SerializedName("order_number_type")
  private int orderNumberType;

  /**
   * 原支付交易对应的微信订单号
   */
  @SerializedName("transaction_id")
  private String transactionId;

  /**
   * 支付下单商户的商户号，由微信支付生成并下发。
   */
  @SerializedName("mchid")
  private String mchId;

  /**
   * 商户系统内部订单号，只能是数字、大小写字母`_-*`且在同一个商户号下唯一
   */
  @SerializedName("out_trade_no")
  private String outTradeNo;

}
