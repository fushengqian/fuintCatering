package com.fuint.repository.bean;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分排行Bean
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@Schema(name="积分排行Bean", description="积分排行Bean")
public class PointRankBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description="会员ID")
    private Integer userId;

    @Schema(description="会员名称")
    private String name;

    @Schema(description="会员头像")
    private String avatar;

    @Schema(description="积分数量")
    private Integer point;

}
