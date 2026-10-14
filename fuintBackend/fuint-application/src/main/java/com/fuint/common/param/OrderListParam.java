package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 订单列表请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class OrderListParam extends PageParam implements Serializable {

    @Schema(description="ID")
    private String id;

    @Schema(description="关键字")
    private String keyword;

    @Schema(description="会员ID")
    private Integer userId;

    @Schema(description="店铺ID，逗号分隔")
    private String storeIds;

    @Schema(description="订单状态")
    private String status;

    @Schema(description="支付状态")
    private String payStatus;

    @Schema(description="结算状态")
    private String settleStatus;

    @Schema(description="核销状态")
    private String confirmStatus;

    @Schema(description="数据类型，1）toPay：待支付；2）paid：已支付；3）cancel：已取消")
    private String dataType;

    @Schema(description="支付类型")
    private List<String> payType;

    @Schema(description="订单类型")
    private String type;

    @Schema(description="订单号")
    private String orderSn;

    @Schema(description="桌码")
    private String tableCode;

    @Schema(description="会员手机号")
    private String mobile;

    @Schema(description="订单模式，1）oneself：自取，2）express：配送")
    private String orderMode;

    @Schema(description="员工ID（销售人员）")
    private String staffId;

    @Schema(description="卡券ID")
    private String couponId;

    @Schema(description="时间类型")
    private String timeType;

    @Schema(description="开始时间")
    private String startTime;

    @Schema(description="结束时间")
    private String endTime;

}
