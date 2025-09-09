package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 易宝支付通知日志表
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Getter
@Setter
@TableName("mt_yeepay_notification_log")
@ApiModel(value = "MtYeepayNotificationLog对象", description = "易宝支付通知日志表")
public class MtYeepayNotificationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("关联的订单ID")
    private String orderId;

    @ApiModelProperty("完整的原始通知数据")
    private String notificationData;

    @ApiModelProperty("处理状态，如'PROCESSED'、'FAILED'")
    private String processStatus;

    @ApiModelProperty("错误信息")
    private String errorMessage;

    @ApiModelProperty("通知接收时间")
    private Date createTime;

    @ApiModelProperty("处理更新时间")
    private Date updateTime;
}