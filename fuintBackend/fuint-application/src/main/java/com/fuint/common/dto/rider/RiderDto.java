package com.fuint.common.dto.rider;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 骑手信息 DTO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RiderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("骑手ID")
    private Integer id;

    @ApiModelProperty("会员用户ID")
    private Integer userId;

    @ApiModelProperty("骑手编号")
    private String riderNo;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("头像")
    private String avatar;

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

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("配送订单总数")
    private Integer totalOrders;

    @ApiModelProperty("配送总收入")
    private BigDecimal totalIncome;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
