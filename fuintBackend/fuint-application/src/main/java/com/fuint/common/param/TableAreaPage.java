package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 桌码区域分页查询参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class TableAreaPage extends PageParam implements Serializable {

    @ApiModelProperty("区域名称")
    private String name;

    @ApiModelProperty("状态")
    private String status;
}
