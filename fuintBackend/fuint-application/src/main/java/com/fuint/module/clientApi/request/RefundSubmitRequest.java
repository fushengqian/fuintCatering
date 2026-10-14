package com.fuint.module.clientApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 售后列表请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RefundSubmitRequest implements Serializable {

    @Schema(description="会员ID")
    private Integer userId;

    @Schema(description="订单ID")
    private Integer orderId;

    @Schema(description="备注信息")
    private String remark;

    @Schema(description="售后类型")
    private String type;

    @Schema(description="售后图片")
    private List<String> images;

}
