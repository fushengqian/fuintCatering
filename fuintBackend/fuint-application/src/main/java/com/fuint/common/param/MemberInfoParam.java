package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 会员查询请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class MemberInfoParam extends PageParam implements Serializable {

    @Schema(description="ID")
    private Integer id;

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="头像")
    private String avatar;

    @Schema(description="称呼")
    private String name;

    @Schema(description="性别")
    private Integer sex;

    @Schema(description="生日")
    private String birthday;

    @Schema(description="会员号")
    private String userNo;

}
