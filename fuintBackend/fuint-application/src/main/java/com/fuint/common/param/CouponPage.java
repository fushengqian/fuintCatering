package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 卡券分页查询参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class CouponPage extends PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("卡券名称")
    private String name;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("分组ID")
    private Integer groupId;

    @ApiModelProperty("卡券类型")
    private String type;

}
