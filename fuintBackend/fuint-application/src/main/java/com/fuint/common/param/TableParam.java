package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 桌台请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class TableParam implements Serializable {

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="区域ID")
    private Integer areaId;

    @Schema(description="使用状态")
    private String useStatus;

    @Schema(description="会员手机号")
    private String mobile;

    @Schema(description="会员号")
    private String userNo;

    @Schema(description="桌码")
    private String code;

}
