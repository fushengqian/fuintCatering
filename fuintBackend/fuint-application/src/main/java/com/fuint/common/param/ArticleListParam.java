package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 文章列表请求参数
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class ArticleListParam extends PageParam implements Serializable {

    @Schema(description="文章标题")
    private String title;

    @Schema(description="商户号")
    private String merchantNo;

}
