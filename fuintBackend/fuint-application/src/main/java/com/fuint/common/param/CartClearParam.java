package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 删除购物车请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class CartClearParam extends PageParam implements Serializable {

    @Schema(description="购物车ID")
    private List<String> cartId;

    @Schema(description="桌台ID")
    private Integer tableId;

    @Schema(description="下单会员ID")
    private Integer userId;

}
