package com.fuint.common.dto.rider;

import com.fuint.common.dto.order.OrderGoodsDto;
import com.fuint.common.dto.order.OrderUserDto;
import com.fuint.repository.model.MtStore;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description="订单ID")
    private Integer orderId;

    @Schema(description="订单号")
    private String orderSn;

    @Schema(description="订单状态")
    private String status;

    @Schema(description="订单状态文本")
    private String statusText;

    @Schema(description="订单金额")
    private BigDecimal amount;

    @Schema(description="配送费")
    private BigDecimal deliveryFee;

    @Schema(description="支付金额")
    private BigDecimal payAmount;

    @Schema(description="用户备注")
    private String remark;

    @Schema(description="下单时间")
    private String createTime;

    @Schema(description="支付时间")
    private String payTime;

    @Schema(description="接单时间")
    private String acceptTime;

    @Schema(description="取货时间")
    private String pickupTime;

    @Schema(description="送达时间")
    private String deliverTime;

    @Schema(description="客户信息")
    private OrderUserDto userInfo;

    @Schema(description="配送地址")
    private String userAddress;

    @Schema(description="店铺信息")
    private MtStore storeInfo;

    @Schema(description="商品列表")
    private List<OrderGoodsDto> goods;

    @Schema(description="取餐码")
    private String verifyCode;

    @Schema(description="取货地址(店铺地址)")
    private String storeAddress;
}
