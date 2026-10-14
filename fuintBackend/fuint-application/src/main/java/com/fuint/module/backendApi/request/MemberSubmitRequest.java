package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 提交会员信息请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class MemberSubmitRequest implements Serializable {

    @Schema(description="会员ID")
    private Integer id;

    @Schema(description="名称")
    private String name;

    @Schema(description="会员等级")
    private Integer gradeId;

    @Schema(description="分组ID")
    private Integer groupId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="会员号")
    private String userNo;

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="性别")
    private Integer sex;

    @Schema(description="身份证")
    private String idcard;

    @Schema(description="生日")
    private String birthday;

    @Schema(description="地址")
    private String address;

    @Schema(description="备注信息")
    private String description;

    @Schema(description="状态")
    private String status;

    @Schema(description="会员开始时间")
    private String startTime;

    @Schema(description="会员结束时间")
    private String endTime;

    @Schema(description="标签ID列表，多个用逗号分隔")
    private String tagIds;

}
