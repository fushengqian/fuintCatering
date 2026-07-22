package com.fuint.common.dto.rider;

import com.fuint.common.dto.order.OrderGoodsDto;
import com.fuint.common.dto.order.OrderUserDto;
import com.fuint.repository.model.MtStore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 骑手订单 DTO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RiderOrderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单ID")
    private Integer orderId;

    @ApiModelProperty("订单号")
    private String orderSn;

    @ApiModelProperty("订单状态")
    private String status;

    @ApiModelProperty("订单状态文本")
    private String statusText;

    @ApiModelProperty("订单金额")
    private BigDecimal amount;

    @ApiModelProperty("配送费")
    private BigDecimal deliveryFee;

    @ApiModelProperty("支付金额")
    private BigDecimal payAmount;

    @ApiModelProperty("用户备注")
    private String remark;

    @ApiModelProperty("下单时间")
    private String createTime;

    @ApiModelProperty("支付时间")
    private String payTime;

    @ApiModelProperty("接单时间")
    private String acceptTime;

    @ApiModelProperty("取货时间")
    private String pickupTime;

    @ApiModelProperty("送达时间")
    private String deliverTime;

    @ApiModelProperty("客户信息")
    private OrderUserDto userInfo;

    @ApiModelProperty("配送地址")
    private String userAddress;

    @ApiModelProperty("店铺信息")
    private MtStore storeInfo;

    @ApiModelProperty("商品列表")
    private List<OrderGoodsDto> goods;

    @ApiModelProperty("取餐码")
    private String verifyCode;

    @ApiModelProperty("取货地址(店铺地址)")
    private String storeAddress;
}
