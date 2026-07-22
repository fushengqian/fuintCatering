package com.fuint.common.dto.rider;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 骑手统计 DTO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RiderStatsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("今日接单数")
    private Integer todayOrders;

    @ApiModelProperty("配送中订单数")
    private Integer deliveringOrders;

    @ApiModelProperty("已完成订单数")
    private Integer completedOrders;

    @ApiModelProperty("总配送订单数")
    private Integer totalOrders;

    @ApiModelProperty("总收入")
    private BigDecimal totalIncome;

    @ApiModelProperty("日均单量")
    private BigDecimal avgDailyOrders;

    @ApiModelProperty("每日配送单量趋势")
    private List<Map<String, Object>> dailyDeliveryList;

    @ApiModelProperty("每日收入趋势")
    private List<Map<String, Object>> dailyIncomeList;
}
