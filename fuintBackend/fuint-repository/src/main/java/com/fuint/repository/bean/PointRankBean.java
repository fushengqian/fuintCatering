package com.fuint.repository.bean;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 积分排行Bean
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "积分排行Bean", description = "积分排行Bean")
public class PointRankBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("会员ID")
    private Integer userId;

    @ApiModelProperty("会员名称")
    private String name;

    @ApiModelProperty("会员头像")
    private String avatar;

    @ApiModelProperty("积分数量")
    private Integer point;

}
