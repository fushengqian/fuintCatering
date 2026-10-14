package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 发放卡券请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class SendCouponParam implements Serializable {

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="发放数量")
    private String num;

    @Schema(description="卡券ID")
    private String couponId;

    @Schema(description="会员ID")
    private String userIds;

    @Schema(description="发放对象")
    private String object;

}
