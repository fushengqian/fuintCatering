package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value="订单商品ID", name="id")
    private Integer id;

    @ApiModelProperty(value="商品ID", name="goodsId")
    private Integer goodsId;

}
