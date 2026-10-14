package com.fuint.module.backendApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 提交商户请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class MerchantSubmitRequest implements Serializable {

    @Schema(description="商户ID")
    private Integer id;

    @Schema(description="商户名称")
    private String name;

    @Schema(description="商户号")
    private String no;

    @Schema(description="联系人")
    private String contact;

    @Schema(description="手机号")
    private String phone;

    @Schema(description="备注信息")
    private String description;

    @Schema(description="地址")
    private String address;

    @Schema(description="状态")
    private String status;

    @Schema(description="图片")
    private String logo;

    @Schema(description="类型")
    private String type;

    @Schema(description="小程序appId")
    private String wxAppId;

    @Schema(description="小程序appSecret")
    private String wxAppSecret;

    @Schema(description="公众号appId")
    private String wxOfficialAppId;

    @Schema(description="公众号AppSecret")
    private String wxOfficialAppSecret;

    @Schema(description="结算比例")
    private BigDecimal settleRate;

    @Schema(description="有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Schema(description="有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

}
