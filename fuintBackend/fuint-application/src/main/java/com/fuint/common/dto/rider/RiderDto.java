package com.fuint.common.dto.rider;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description="骑手ID")
    private Integer id;

    @Schema(description="会员用户ID")
    private Integer userId;

    @Schema(description="骑手编号")
    private String riderNo;

    @Schema(description="姓名")
    private String name;

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="头像")
    private String avatar;

    @Schema(description="所属商户ID")
    private Integer merchantId;

    @Schema(description="所属店铺ID")
    private Integer storeId;

    @Schema(description="身份证号")
    private String idCard;

    @Schema(description="身份证正面照片")
    private String idCardFront;

    @Schema(description="身份证背面照片")
    private String idCardBack;

    @Schema(description="健康证照片")
    private String healthCert;

    @Schema(description="状态")
    private String status;

    @Schema(description="配送订单总数")
    private Integer totalOrders;

    @Schema(description="配送总收入")
    private BigDecimal totalIncome;

    @Schema(description="创建时间")
    private Date createTime;
}
