package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 会员列表请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class MemberListParam extends PageParam implements Serializable {

    @Schema(description="ID")
    private String id;

    @Schema(description="商户ID")
    private Integer merchantId;

    @Schema(description="店铺ID")
    private Integer storeId;

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="会员ID")
    private Integer userId;

    @Schema(description="关键字")
    private String keyword;

    @Schema(description="会员名称")
    private String name;

    @Schema(description="会员生日")
    private String birthday;

    @Schema(description="会员号")
    private String userNo;

    @Schema(description="会员等级")
    private String gradeId;

    @Schema(description="注册时间")
    private String regTime;

    @Schema(description="活跃时间")
    private String activeTime;

    @Schema(description="会员有效期")
    private String memberTime;

    @Schema(description="会员开始时间")
    private String startTime;

    @Schema(description="会员结束时间")
    private String endTime;

    @Schema(description="数据类型，1）todayRegister：今日注册；2）todayActive：今日活跃")
    private String dataType;

    @Schema(description="所属店铺ID-多店铺")
    private String storeIds;

    @Schema(description="所属分组ID")
    private String groupIds;

    @Schema(description="会员状态")
    private String status;

}
