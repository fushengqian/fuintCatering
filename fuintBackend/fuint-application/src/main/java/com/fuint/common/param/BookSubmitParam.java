package com.fuint.common.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 预约提交请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class BookSubmitParam implements Serializable {

    @Schema(description="预约ID")
    private String bookId;

    @Schema(description="订单商品ID")
    private String orderGoodsId;

    @Schema(description="员工ID")
    private String staffId;

    @Schema(description="备注")
    private String remark;

    @Schema(description="手机号")
    private String mobile;

    @Schema(description="联系人")
    private String contact;

    @Schema(description="预约日期")
    private String date;

    @Schema(description="预约时间")
    private String time;

}
