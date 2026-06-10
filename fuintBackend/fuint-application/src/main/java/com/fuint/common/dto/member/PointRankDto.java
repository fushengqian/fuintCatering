package com.fuint.common.dto.member;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 积分排行DTO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class PointRankDto implements Serializable {

    @ApiModelProperty("会员ID")
    private Integer userId;

    @ApiModelProperty("会员称呼")
    private String name;

    @ApiModelProperty("会员头像")
    private String avatar;

    @ApiModelProperty("积分数量")
    private Integer point;

}
