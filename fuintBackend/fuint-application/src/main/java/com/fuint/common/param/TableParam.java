package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value="商户ID", name="merchantId")
    private Integer merchantId;

    @ApiModelProperty(value="店铺ID", name="storeId")
    private Integer storeId;

    @ApiModelProperty(value="区域ID", name="areaId")
    private Integer areaId;

    @ApiModelProperty(value="使用状态", name="useStatus")
    private String useStatus;

    @ApiModelProperty(value="会员手机号", name="mobile")
    private String mobile;

    @ApiModelProperty(value="会员号", name="userNo")
    private String userNo;

    @ApiModelProperty(value="桌码", name="code")
    private String code;

}
