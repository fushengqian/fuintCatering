package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 分佣提成结算请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class CommissionSettleRequest implements Serializable {

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="员工姓名")
    private String realName;

    @Schema(description="员工手机号")
    private String mobile;

    @Schema(description="开始时间")
    private String startTime;

    @Schema(description="结束时间")
    private String endTime;

    @Schema(description="操作人")
    private String operator;

}
