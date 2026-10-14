package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 售后订单信息请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RefundInfoParam extends PageParam implements Serializable {

    @Schema(description="售后单ID")
    private Integer refundId;

    @Schema(description="售后状态")
    private String status;

    @Schema(description="备注信息")
    private String remark;

    @Schema(description="拒绝原因")
    private String rejectReason;

    @Schema(description="物流公司名称")
    private String expressName;

    @Schema(description="物流单号")
    private String expressNo;

}
