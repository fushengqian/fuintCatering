package com.fuint.common.dto.cashier;

import com.fuint.common.dto.order.UserOrderDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 桌台详情实体类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class TableDetail {

    @Schema(description="桌台ID")
    private Integer tableId;

    @Schema(description="桌台编码")
    private String tableCode;

    @Schema(description="使用状态")
    private UserOrderDto orderInfo;

}
