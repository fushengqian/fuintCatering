package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 骑手表
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@TableName("mt_rider")
@Schema(name="MtRider对象", description="骑手表")
public class MtRider implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description="自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @Schema(description="会员用户ID，关联mt_user")
    private Integer userId;

    @Schema(description="骑手编号")
    private String riderNo;

    @Schema(description="所属商户ID")
    private Integer merchantId;

    @Schema(description="所属店铺ID")
    private Integer storeId;

    @Schema(description="身份证号")
    private String idCard;

    @Schema(description="身份证正面照片")
    private String idCardFront;

    @Schema(description="身份证背面照片")
    private String idCardBack;

    @Schema(description="健康证照片")
    private String healthCert;

    @Schema(description="状态：A=正常 B=禁用")
    private String status;

    @Schema(description="备注")
    private String description;

    @Schema(description="创建时间")
    private Date createTime;

    @Schema(description="更新时间")
    private Date updateTime;
}
