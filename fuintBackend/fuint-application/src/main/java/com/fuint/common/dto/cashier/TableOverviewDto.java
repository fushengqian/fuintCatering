package com.fuint.common.dto.cashier;

import com.fuint.repository.model.MtUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 桌台概况实体
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 * */
@Data
public class TableOverviewDto implements Serializable {

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="桌码")
    private String code;

    @Schema(description="人数")
    private Integer people;

    @Schema(description="已占用分钟数")
    private Integer minute;

    @Schema(description="金额")
    private Integer amount;

    @Schema(description="会员信息")
    private MtUser userInfo;

    @Schema(description="状态，1：空闲；2：占用")
    private String status;

}
