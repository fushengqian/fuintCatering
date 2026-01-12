package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 桌码实体
 *
 * @Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@TableName("mt_table")
@ApiModel(value = "table表对象", description = "table表对象")
public class MtTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("桌子编码")
    private String code;

    @ApiModelProperty("区域ID")
    private Integer areaId;

    @ApiModelProperty("所属商户ID")
    private Integer merchantId;

    @ApiModelProperty("所属店铺ID")
    private Integer storeId;

    @ApiModelProperty("人数上限")
    private Integer maxPeople;

    @ApiModelProperty("备注信息")
    private String description;

    @ApiModelProperty("使用状态")
    private String useStatus;

    @ApiModelProperty("开台时间")
    private Date useTime;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("最后操作人")
    private String operator;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("A：正常；D：删除")
    private String status;

}
