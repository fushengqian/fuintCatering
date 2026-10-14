package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 订单核销请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class OrderConfirmParam implements Serializable {

    @Schema(description="核销码")
    private String code;

    @Schema(description="订单ID")
    private Integer orderId;

    @Schema(description="核销备注")
    private String remark;

}
