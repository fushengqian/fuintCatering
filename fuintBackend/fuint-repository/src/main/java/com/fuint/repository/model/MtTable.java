package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 桌码实体
 *
 * @Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@TableName("mt_table")
@Schema(name="table表对象", description="table表对象")
public class MtTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description="自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @Schema(description="桌子编码")
    private String code;

    @Schema(description="区域ID")
    private Integer areaId;

    @Schema(description="所属商户ID")
    private Integer merchantId;

    @Schema(description="所属店铺ID")
    private Integer storeId;

    @Schema(description="人数上限")
    private Integer maxPeople;

    @Schema(description="备注信息")
    private String description;

    @Schema(description="使用状态")
    private String useStatus;

    @Schema(description="开台时间")
    private Date useTime;

    @Schema(description="创建时间")
    private Date createTime;

    @Schema(description="更新时间")
    private Date updateTime;

    @Schema(description="最后操作人")
    private String operator;

    @Schema(description="排序")
    private Integer sort;

    @Schema(description="A：正常；D：删除")
    private String status;

}
