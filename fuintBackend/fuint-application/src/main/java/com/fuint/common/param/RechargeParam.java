package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 充值请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RechargeParam implements Serializable {

    @Schema(description="充值金额")
    private String rechargeAmount;

    @Schema(description="自定义充值金额")
    private String customAmount;

    @Schema(description="会员ID")
    private Integer memberId;

}
