package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 卡券转赠请求参数
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class GiveParam implements Serializable {

    @Schema(description="转增对象手机号")
    private String mobile;

    @Schema(description="卡券ID，可逗号隔开")
    private String couponId;

    @Schema(description="转赠备注")
    private String note;

    @Schema(description="转赠留言")
    private String message;

    @Schema(description="转赠人ID")
    private Integer userId;

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺ID")
    private Integer storeId;

}
