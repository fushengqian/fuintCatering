package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 后台账号详情
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class AccountInfoRequest implements Serializable {

    @Schema(description="账号ID")
    private Integer id;

    @Schema(description="用户名")
    private String accountName;

    @Schema(description="密码")
    private String password;

    @Schema(description="密码加密")
    private String salt;

    @Schema(description="状态")
    private String accountStatus;

    @Schema(description="角色ID，逗号隔开")
    private String roleIds;

    @Schema(description="真实姓名")
    private String realName;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="员工ID")
    private Integer staffId;
}
