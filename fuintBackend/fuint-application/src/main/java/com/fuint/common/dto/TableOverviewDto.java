package com.fuint.common.dto;

import com.fuint.repository.model.MtUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * 桌台概况实体
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 * */
@Getter
@Setter
public class TableOverviewDto implements Serializable {

    @ApiModelProperty("店铺ID")
    private Integer storeId;

    @ApiModelProperty("桌码")
    private String code;

    @ApiModelProperty("人数")
    private Integer people;

    @ApiModelProperty("已占用分钟数")
    private Integer minute;

    @ApiModelProperty("金额")
    private Integer amount;

    @ApiModelProperty("会员信息")
    private MtUser userInfo;

    @ApiModelProperty("状态，1：空闲；2：占用")
    private String status;

}
