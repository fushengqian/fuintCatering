package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 取消商品请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class RemoveGoodsParam implements Serializable {

    @Schema(description="订单商品ID")
    private Integer id;

    @Schema(description="商品ID")
    private Integer goodsId;

}
