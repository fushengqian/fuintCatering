package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 骑手配送记录表
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@TableName("mt_rider_order")
@ApiModel(value = "MtRiderOrder对象", description = "骑手配送记录表")
public class MtRiderOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("订单ID")
    private Integer orderId;

    @ApiModelProperty("骑手ID")
    private Integer riderId;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("店铺ID")
    private Integer storeId;

    @ApiModelProperty("配送状态")
    private String status;

    @ApiModelProperty("接单时间")
    private Date acceptTime;

    @ApiModelProperty("取货时间")
    private Date pickupTime;

    @ApiModelProperty("送达时间")
    private Date deliverTime;

    @ApiModelProperty("完成时间")
    private Date completeTime;

    @ApiModelProperty("骑手配送费")
    private BigDecimal deliveryFee;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}
