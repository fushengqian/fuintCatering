package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 骑手-我的配送订单查询参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiderMyOrderParam extends PageParam {

    @ApiModelProperty("订单状态，逗号分隔多个状态")
    private String status;

}
