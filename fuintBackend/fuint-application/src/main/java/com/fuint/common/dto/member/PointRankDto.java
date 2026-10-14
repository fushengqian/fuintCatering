package com.fuint.common.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 积分排行DTO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class PointRankDto implements Serializable {

    @Schema(description="会员ID")
    private Integer userId;

    @Schema(description="会员称呼")
    private String name;

    @Schema(description="会员头像")
    private String avatar;

    @Schema(description="积分数量")
    private Integer point;

}
