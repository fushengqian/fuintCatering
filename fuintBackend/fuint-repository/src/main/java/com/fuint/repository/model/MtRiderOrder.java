package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 骑手配送记录表
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@TableName("mt_rider_order")
@Schema(name="MtRiderOrder对象", description="骑手配送记录表")
public class MtRiderOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description="自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @Schema(description="订单ID")
    private Integer orderId;

    @Schema(description="骑手ID")
    private Integer riderId;

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="配送状态")
    private String status;

    @Schema(description="接单时间")
    private Date acceptTime;

    @Schema(description="取货时间")
    private Date pickupTime;

    @Schema(description="送达时间")
    private Date deliverTime;

    @Schema(description="完成时间")
    private Date completeTime;

    @Schema(description="骑手配送费")
    private BigDecimal deliveryFee;

    @Schema(description="备注")
    private String remark;

    @Schema(description="创建时间")
    private Date createTime;

    @Schema(description="更新时间")
    private Date updateTime;
}
