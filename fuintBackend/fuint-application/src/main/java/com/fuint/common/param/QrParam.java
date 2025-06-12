package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 二维码请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class QrParam implements Serializable {

    @ApiModelProperty(value="ID", name="id")
    private Integer id;

    @ApiModelProperty(value="二维码类型", name="type")
    private String type;

    @ApiModelProperty(value="二维码内容", name="content")
    private String content;

}
