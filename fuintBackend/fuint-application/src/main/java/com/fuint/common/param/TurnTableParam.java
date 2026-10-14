package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description="新桌台ID")
    private Integer tableId;

    @Schema(description="被转桌台ID")
    private Integer turnTableId;
}
