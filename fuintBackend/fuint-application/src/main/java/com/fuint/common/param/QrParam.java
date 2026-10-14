package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description="ID")
    private Integer id;

    @Schema(description="二维码类型")
    private String type;

    @Schema(description="二维码内容")
    private String content;

}
