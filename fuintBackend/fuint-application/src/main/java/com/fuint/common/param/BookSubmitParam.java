package com.fuint.common.param;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value="预约ID", name="bookId")
    private String bookId;

    @ApiModelProperty(value="订单商品ID", name="orderGoodsId")
    private String orderGoodsId;

    @ApiModelProperty(value="员工ID", name="staffId")
    private String staffId;

    @ApiModelProperty(value="备注", name="remark")
    private String remark;

    @ApiModelProperty(value="手机号", name="mobile")
    private String mobile;

    @ApiModelProperty(value="联系人", name="contact")
    private String contact;

    @ApiModelProperty(value="预约日期", name="date")
    private String date;

    @ApiModelProperty(value="预约时间", name="time")
    private String time;

}
