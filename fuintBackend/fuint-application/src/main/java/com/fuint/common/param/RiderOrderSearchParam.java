package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 骑手-已完成订单搜索参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiderOrderSearchParam extends PageParam {

    @Schema(description="订单号")
    private String orderSn;

    @Schema(description="开始时间 yyyy-MM-dd HH:mm:ss")
    private String beginTime;

    @Schema(description="结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

}
