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
 * 骑手表
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@TableName("mt_rider")
@ApiModel(value = "MtRider对象", description = "骑手表")
public class MtRider implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("会员用户ID，关联mt_user")
    private Integer userId;

    @ApiModelProperty("骑手编号")
    private String riderNo;

    @ApiModelProperty("所属商户ID")
    private Integer merchantId;

    @ApiModelProperty("所属店铺ID")
    private Integer storeId;

    @ApiModelProperty("身份证号")
    private String idCard;

    @ApiModelProperty("身份证正面照片")
    private String idCardFront;

    @ApiModelProperty("身份证背面照片")
    private String idCardBack;

    @ApiModelProperty("健康证照片")
    private String healthCert;

    @ApiModelProperty("状态：A=正常 B=禁用")
    private String status;

    @ApiModelProperty("备注")
    private String description;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
