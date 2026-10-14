package com.fuint.common.dto.rider;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description="今日接单数")
    private Integer todayOrders;

    @Schema(description="配送中订单数")
    private Integer deliveringOrders;

    @Schema(description="已完成订单数")
    private Integer completedOrders;

    @Schema(description="总配送订单数")
    private Integer totalOrders;

    @Schema(description="总收入")
    private BigDecimal totalIncome;

    @Schema(description="日均单量")
    private BigDecimal avgDailyOrders;

    @Schema(description="每日配送单量趋势")
    private List<Map<String, Object>> dailyDeliveryList;

    @Schema(description="每日收入趋势")
    private List<Map<String, Object>> dailyIncomeList;
}
