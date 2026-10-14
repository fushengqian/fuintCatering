package com.fuint.module.clientApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 会员信息请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class MemberInfoRequest implements Serializable {

    @Schema(description="会员名称")
    private String name;

    @Schema(description="会员生日")
    private String birthday;

    @Schema(description="头像url")
    private String avatar;

    @Schema(description="性别")
    private Integer sex;

    @Schema(description="微信手机号授权码")
    private String code;

    @Schema(description="微信手机号授权加密数据")
    private String encryptedData;

    @Schema(description="微信手机号授权码iv")
    private String iv;

    @Schema(description="新密码")
    private String password;

    @Schema(description="旧密码")
    private String passwordOld;

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="验证码")
    private String verifyCode;

}
