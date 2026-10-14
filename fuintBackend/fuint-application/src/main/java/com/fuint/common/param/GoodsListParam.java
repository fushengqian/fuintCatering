package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 商品列表请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class GoodsListParam extends PageParam implements Serializable {

    @Schema(description="商品名称")
    private String name;

    @Schema(description="商品编码")
    private String goodsNo;

    @Schema(description="搜索关键字")
    private String keyword;

    @Schema(description="是否单规格")
    private String isSingleSpec;

    @Schema(description="商品类型")
    private String type;

    @Schema(description="可用平台")
    private String platform;

    @Schema(description="商品状态")
    private String status;

    @Schema(description="是否有库存")
    private String stock;

    @Schema(description="商品分类")
    private Integer cateId;

    @Schema(description="是否有价格")
    private String hasPrice;

    @Schema(description="积分商品筛选，Y：只查积分兑换商品，N或不传：只查非积分商品")
    private String pointGoods;

    @Schema(description="排序方式")
    private String sortType;

    @Schema(description="按价格排序")
    private String sortPrice;

}
