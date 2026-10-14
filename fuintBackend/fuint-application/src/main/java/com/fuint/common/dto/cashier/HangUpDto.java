package com.fuint.common.dto.cashier;

import com.fuint.repository.model.MtTable;
import com.fuint.repository.model.MtUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收银挂单实体类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class HangUpDto {

    @Schema(description="桌台ID")
    private Integer tableId;

    @Schema(description="桌台编码")
    private String tableCode;

    @Schema(description="是否空闲")
    private Boolean isEmpty;

    @Schema(description="会员信息")
    private MtUser memberInfo;

    @Schema(description="件数")
    private Double num;

    @Schema(description="金额")
    private BigDecimal amount;

    @Schema(description="时间")
    private String dateTime;

    @Schema(description="使用状态")
    private String useStatus;

    @Schema(description="使用时长")
    private String useTime;

    @Schema(description="桌台")
    private MtTable tableInfo;

}
