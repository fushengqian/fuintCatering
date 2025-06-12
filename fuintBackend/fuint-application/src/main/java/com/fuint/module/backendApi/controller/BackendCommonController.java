package com.fuint.module.backendApi.controller;

import com.fuint.common.dto.AccountInfo;
import com.fuint.common.enums.QrCodeEnum;
import com.fuint.common.param.QrParam;
import com.fuint.common.service.*;
import com.fuint.common.util.Base64Util;
import com.fuint.common.util.QRCodeUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtStore;
import com.fuint.repository.model.MtTable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台公共接口控制器
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-公共接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/common")
public class BackendCommonController extends BaseController {

    private Environment env;

    /**
     * 微信服务接口
     * */
    private WeixinService weixinService;

    /**
     * 系统设置服务接口
     * */
    private SettingService settingService;

    /**
     * 店铺服务接口
     * */
    private StoreService storeService;

    /**
     * 桌码服务接口
     */
    private TableService tableService;

    /**
     * 卡券服务接口
     */
    private CouponService couponService;

    /**
     * 生成二维码
     *
     * @return
     */
    @ApiOperation(value = "生成二维码")
    @RequestMapping(value = "/createQrCode", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject createQrCode(HttpServletRequest request, @RequestBody QrParam qrParam) throws Exception {
        String type = qrParam.getType() != null ? qrParam.getType() : "";
        Integer id = qrParam.getId() == null ? 0 : qrParam.getId();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));
        Integer merchantId = accountInfo.getMerchantId();
        String page = QrCodeEnum.STORE.getPage() + "?" + QrCodeEnum.STORE.getKey() + "Id=" + id;
        if (type.equals(QrCodeEnum.TABLE.getKey())) {
            page = QrCodeEnum.TABLE.getPage() + "?" + QrCodeEnum.TABLE.getKey() + "Id=" + id;
            MtTable mtTable = tableService.queryTableById(id);
            if (mtTable != null) {
                merchantId = mtTable.getMerchantId();
            }
        }
        if (type.equals(QrCodeEnum.COUPON.getKey())) {
            page = QrCodeEnum.COUPON.getPage() + "?" + QrCodeEnum.COUPON.getKey() + "Id=" + id;
        }
        if (type.equals(QrCodeEnum.STORE.getKey())) {
            MtStore mtStore = storeService.queryStoreById(id);
            if (mtStore != null) {
                merchantId = mtStore.getMerchantId();
            }
        }
        if (type.equals(QrCodeEnum.COUPON.getKey())) {
            MtCoupon mtCoupon = couponService.queryCouponById(id);
            if (mtCoupon != null) {
                merchantId = mtCoupon.getMerchantId();
            }
        }
        String h5QrCode = "";
        String h5Page = env.getProperty("website.url") + "#" + page;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCodeUtil.createQrCode(out, h5Page, 800, 800, "png", "");
        h5QrCode = new String(Base64Util.baseEncode(out.toByteArray()), "UTF-8");
        h5QrCode = "data:image/jpg;base64," + h5QrCode;

        String imagePath = settingService.getUploadBasePath();
        String minAppQrCode = weixinService.createQrCode(merchantId, type, id, page, 320);
        minAppQrCode = imagePath + minAppQrCode;

        Map<String, Object> result = new HashMap<>();
        result.put("minAppQrCode", minAppQrCode);
        result.put("h5QrCode", h5QrCode);

        return getSuccessResult(result);
    }

    /**
     * 生成条形码
     *
     * @return
     */
    @ApiOperation(value = "生成条形码")
    @RequestMapping(value = "/createBarCode", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject createBarCode(@RequestBody QrParam qrParam) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCodeUtil.createBarCode(out, qrParam.getContent(), 800, 800);

        String barcode = new String(Base64Util.baseEncode(out.toByteArray()), "UTF-8");
        barcode = "data:image/jpg;base64," + barcode;

        return getSuccessResult(barcode);
    }
}
