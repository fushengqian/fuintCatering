package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 骑手-接单池查询参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiderOrderPoolParam extends PageParam {

    @ApiModelProperty("店铺ID，可选过滤")
    private Integer storeId;

}
