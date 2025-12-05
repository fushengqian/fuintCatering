package com.fuint.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 桌台详情实体类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class TableDetail {

    @ApiModelProperty("桌台ID")
    private Integer tableId;

    @ApiModelProperty("桌台编码")
    private String tableCode;

    @ApiModelProperty("使用状态")
    private UserOrderDto orderInfo;

}
