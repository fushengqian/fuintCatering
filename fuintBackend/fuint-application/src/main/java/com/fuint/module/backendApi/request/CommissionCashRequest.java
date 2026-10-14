package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 分佣提成提现请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class CommissionCashRequest implements Serializable {

    @Schema(description="ID")
    private Integer id;

    @Schema(description="分佣金额")
    private String amount;

    @Schema(description="备注说明")
    private String description;

    @Schema(description="状态")
    private String status;

    @Schema(description="最后操作人")
    private String operator;
}
