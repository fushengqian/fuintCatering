package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * 已发短信分页请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class SmsPage extends PageParam implements Serializable {

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("状态")
    private String status;

}
