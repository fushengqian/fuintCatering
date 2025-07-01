package com.fuint.module.backendApi.request;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value="商户ID", name="merchantId")
    private BigDecimal amount;

    @ApiModelProperty(value="会员ID", name="userId")
    private Integer userId;

    @ApiModelProperty(value="备注说明", name="remark")
    private String remark;

    @ApiModelProperty(value="类型，1 增加，2 扣减", name="type")
    private Integer type;
}
