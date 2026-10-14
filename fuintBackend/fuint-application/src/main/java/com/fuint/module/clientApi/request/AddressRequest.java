package com.fuint.module.clientApi.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

/**
 * 请求收货地址请求参数
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
public class AddressRequest implements Serializable {

    @Schema(description="收货地址ID")
    private Integer addressId;

    @Schema(description="收货人姓名")
    private String name;

    @Schema(description="收货人手机号")
    private String mobile;

    @Schema(description="省份ID")
    private Integer provinceId;

    @Schema(description="城市ID")
    private Integer cityId;

    @Schema(description="地区ID")
    private Integer regionId;

    @Schema(description="详细地址")
    private String detail;

    @Schema(description="状态")
    private String status;

    @Schema(description="是否默认地址")
    private String isDefault;

}
