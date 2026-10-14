package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 订单列表请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class StaffParam extends PageParam implements Serializable {

    @Schema(description="ID")
    private Integer id;

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="类别")
    private Integer category;

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="真实姓名")
    private String realName;

    @Schema(description="头像")
    private String avatar;

    @Schema(description="备注信息")
    private String description;

    @Schema(description="审核状态")
    private String auditedStatus;

}
