package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 保存购物车请求参数
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class CartSaveParam implements Serializable {

    @Schema(description="购物车ID")
    private Integer cartId;

    @Schema(description="商品ID")
    private Integer goodsId;

    @Schema(description="商品SkuID")
    private Integer skuId;

    @Schema(description="商品编码")
    private String skuNo;

    @Schema(description="购买数量")
    private Double buyNum;

    @Schema(description="操作类型，+：增加，-：减少")
    private String action;

    @Schema(description="桌台ID")
    private Integer tableId;

    @Schema(description="下单会员ID")
    private Integer userId;

}
