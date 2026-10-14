package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 后台充值请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RechargeRequest implements Serializable {

    @Schema(description="商户ID")
    private BigDecimal amount;

    @Schema(description="会员ID")
    private Integer userId;

    @Schema(description="备注说明")
    private String remark;

    @Schema(description="类型，1 增加，2 扣减")
    private Integer type;
}
