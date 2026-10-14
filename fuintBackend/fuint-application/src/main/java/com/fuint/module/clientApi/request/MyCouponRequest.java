package com.fuint.module.clientApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 我的卡券请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class MyCouponRequest implements Serializable {

    @Schema(description="卡券ID")
    private Integer id;

    @Schema(description="会员卡券ID")
    private Integer userCouponId;

}
