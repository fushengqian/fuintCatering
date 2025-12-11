package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 转台请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class TurnTableParam implements Serializable {

    @ApiModelProperty(value="新桌台ID", name="tableId")
    private Integer tableId;

    @ApiModelProperty(value="被转桌台ID", name="turnTableId")
    private Integer turnTableId;
}
