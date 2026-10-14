package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 卡券列表请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class CouponListParam extends PageParam implements Serializable {

    @Schema(description="关键字")
    private String keyword;

    @Schema(description="卡券类型")
    private String type;

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="领取所需积分")
    private Integer needPoint;

    @Schema(description="发放方式")
    private String sendWay;

    @Schema(description="排序类型")
    private String sortType;

    @Schema(description="下单会员ID")
    private Integer userId;

    @Schema(description="面额排序")
    private String sortPrice;

    @Schema(description="状态")
    private String status;

}
