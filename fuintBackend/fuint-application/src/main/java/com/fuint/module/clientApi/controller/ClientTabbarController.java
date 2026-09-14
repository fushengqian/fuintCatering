package com.fuint.module.clientApi.controller;

import com.fuint.common.dto.decorate.TabbarDto;
import com.fuint.common.service.MerchantService;
import com.fuint.common.service.PageDecorateService;
import com.fuint.common.service.StoreService;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtStore;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 会员端底部导航接口controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="会员端-底部导航相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/clientApi/tabbar")
public class ClientTabbarController extends BaseController {

    /**
     * 页面装修服务接口
     */
    private PageDecorateService pageDecorateService;

    /**
     * 商户服务接口
     */
    private MerchantService merchantService;

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 获取底部导航配置
     */
    @ApiOperation(value = "获取底部导航配置")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject info(HttpServletRequest request) {
        String merchantNo = request.getHeader("merchantNo") == null ? "" : request.getHeader("merchantNo");
        Integer storeId = StringUtil.isEmpty(request.getHeader("storeId")) ? 0 : Integer.parseInt(request.getHeader("storeId"));
        Integer merchantId = merchantService.getMerchantId(merchantNo);
        // 门店可跨商户切换:storeId 能唯一确定门店所属商户,优先按门店所属商户取配置。
        // 否则切换到其它商户的门店时,前端 merchantNo 尚未更新,会返回上一家商户的导航
        if (storeId > 0) {
            MtStore mtStore = storeService.queryStoreById(storeId);
            if (mtStore != null && mtStore.getMerchantId() != null && mtStore.getMerchantId() > 0) {
                merchantId = mtStore.getMerchantId();
            }
        }

        TabbarDto tabbarDto = pageDecorateService.getTabbar(merchantId, storeId);
        if (tabbarDto == null) {
            tabbarDto = new TabbarDto();
        }
        return getSuccessResult(tabbarDto);
    }
}
