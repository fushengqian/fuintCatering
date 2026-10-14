package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 后台登录请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class SettlementRequest implements Serializable {

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="结算ID")
    private Integer settlementId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="下单开始时间")
    private String startTime;

    @Schema(description="下单结束时间")
    private String endTime;

    @Schema(description="备注说明")
    private String remark;

    @Schema(description="最后操作人")
    private String operator;
}
