package com.fuint.module.clientApi.controller;

import com.fuint.common.dto.common.ParamDto;
import com.fuint.common.dto.member.UserInfo;
import com.fuint.common.dto.merchant.StoreInfo;
import com.fuint.common.enums.OrderSettingEnum;
import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统接口相关controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="会员端-系统配置相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/clientApi/system")
public class ClientSystemController extends BaseController {

    /**
     * 店铺服务接口
     * */
    private StoreService storeService;

    /**
     * 会员服务接口
     */
    private MemberService memberService;

    /**
     * 配置服务接口
     * */
    private SettingService settingService;

    /**
     * 商户接口
     */
    private MerchantService merchantService;

    /**
     * 桌码服务接口
     */
    private TableService tableService;

    /**
     * 微信服务接口
     */
    private WeixinService weixinService;

    /**
     * 获取系统配置
     */
    @ApiOperation(value = "获取系统配置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject config(HttpServletRequest request) throws BusinessCheckException {
        String platform = request.getHeader("platform");
        String merchantNo = request.getHeader("merchantNo") == null ? "" : request.getHeader("merchantNo");
        String storeId = request.getHeader("storeId") == null ? "" : request.getHeader("storeId");
        // 记录请求中显式指定的门店(被桌码兜底覆盖之前),用于判断该门店是否真的可用
        String requestStoreId = storeId;
        String latitude = request.getHeader("latitude") == null ? "" : request.getHeader("latitude");
        String longitude = request.getHeader("longitude") == null ? "" : request.getHeader("longitude");
        String tableId =  request.getHeader("tableId") == null ? "" : request.getHeader("tableId");

        UserInfo loginInfo = TokenUtil.getUserInfo();
        Integer merchantId = merchantService.getMerchantId(merchantNo);

        // 默认店铺，取会员之前选择的店铺
        MtStore mtStore = null;
        MtUser mtUser = null;
        if (loginInfo != null) {
            mtUser = memberService.queryMemberById(loginInfo.getId());
            if (mtUser != null) {
                // 会员已禁用
                if (!mtUser.getStatus().equals(StatusEnum.ENABLED.getKey())) {
                    return getFailureResult(1001);
                }
                // 商户不同
                if (!mtUser.getMerchantId().equals(merchantId)) {
                    return getFailureResult(1001);
                }
            }
        }

        // 扫码下单:桌码所属店铺仅作为兜底,优先级低于显式指定的 storeId。
        // 客户端 tableId 写入后无清理时机,会长期缓存在本地;若其优先级高于 storeId,
        // 扫码切店(URL 带 storeId)会被历史桌码所属店铺覆盖,导致切换门店始终不生效
        if (StringUtil.isEmpty(storeId) || "0".equals(storeId)) {
            if (StringUtil.isNotEmpty(tableId)) {
                MtTable mtTable = tableService.queryTableById(Integer.parseInt(tableId));
                if (mtTable != null && mtTable.getStoreId() != null) {
                    storeId = mtTable.getStoreId().toString();
                }
            }
        }

        // 默认的店铺
        if (StringUtil.isNotEmpty(storeId)) {
            mtStore = storeService.queryStoreById(Integer.parseInt(storeId));
            // 店铺是否已关闭
            if (mtStore != null) {
                if (!mtStore.getStatus().equals(StatusEnum.ENABLED.getKey())) {
                    mtStore = null;
                }
            }
        }

        // 取距离最近的
        if (mtStore == null && StringUtil.isNotEmpty(latitude) && StringUtil.isNotEmpty(longitude)) {
            List<StoreInfo> storeList = storeService.queryByDistance(merchantNo, "", latitude, longitude);
            if (storeList.size() > 0) {
                MtStore store = new MtStore();
                BeanUtils.copyProperties(storeList.get(0), store);
                mtStore = store;
            }
        }

        // 最后取系统默认的店铺
        if (mtStore == null) {
            mtStore = storeService.getDefaultStore(merchantNo);
        }

        // 显式指定了门店但该门店不存在或已停用时,标记给前端提示:
        // 此时已静默回退到最近门店或系统默认门店,不提示会让用户误以为已进入目标门店。
        // 注意:不同门店本就可能属于不同商户,跨商户切店是正常场景
        // (门店所属商户会随 storeInfo.merchantNo 回传并由前端更新),不视为不可用
        boolean storeUnavailable = false;
        if (StringUtil.isNotEmpty(requestStoreId) && !"0".equals(requestStoreId)) {
            try {
                MtStore requestStore = storeService.queryStoreById(Integer.parseInt(requestStoreId));
                if (requestStore == null || !requestStore.getStatus().equals(StatusEnum.ENABLED.getKey())) {
                    storeUnavailable = true;
                }
            } catch (Exception e) {
                storeUnavailable = true;
            }
        }

        // 完善会员的店铺信息
        if (mtUser != null && mtStore != null && (mtUser.getStoreId() == null || mtUser.getStoreId() < 1)) {
            // 门店须与会员同属一个商户:跨商户切店时 mtStore 是其它商户的门店,
            // 直接写入会把会员绑定到别的商户的门店下
            if (mtStore.getMerchantId() != null && mtStore.getMerchantId().equals(mtUser.getMerchantId())) {
                mtUser.setStoreId(mtStore.getId());
                mtUser.setUpdateTime(new Date());
                memberService.updateMember(mtUser, false);
            }
        }

        StoreInfo storeInfo = new StoreInfo();
        if (mtStore != null) {
            BeanUtils.copyProperties(mtStore, storeInfo);
            MtMerchant mtMerchant = merchantService.queryMerchantById(mtStore.getMerchantId());
            if (mtMerchant != null) {
                storeInfo.setMerchantNo(mtMerchant.getNo());
            }
        } else {
            storeInfo = null;
        }

        // 桌码信息
        MtTable tableInfo = null;
        if (StringUtil.isNotEmpty(tableId)) {
            tableInfo = tableService.queryTableById(Integer.parseInt(tableId));
        }

        // 是否单店铺
        storeInfo.setSingle(YesOrNoEnum.YES.getKey());
        if (storeInfo != null) {
            List<MtStore> stores = storeService.getActiveStoreList(storeInfo.getMerchantId(), 0, null);
            if (stores != null && stores.size() > 1) {
                storeInfo.setSingle(YesOrNoEnum.NO.getKey());
            }
        }

        // 支付方式列表
        List<ParamDto> payTypeList = settingService.getPayTypeList(merchantId, (storeInfo == null) ? 0 : storeInfo.getId(), platform);

        // 支付模式：Y先用餐后支付；N先支付后用餐
        MtSetting paySetting = settingService.querySettingByName(merchantId, SettingTypeEnum.ORDER.getKey(), OrderSettingEnum.PAY_FIRST.getKey());
        String payFirst = YesOrNoEnum.YES.getKey();
        if (paySetting != null) {
            payFirst = paySetting.getValue();
        }

        // 显示就餐人数
        MtSetting peopleNumSetting = settingService.querySettingByName(merchantId, SettingTypeEnum.ORDER.getKey(), OrderSettingEnum.PEOPLE_NUM.getKey());
        String peopleNum = YesOrNoEnum.NO.getKey();
        if (peopleNumSetting != null) {
            peopleNum = peopleNumSetting.getValue();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("storeInfo", storeInfo);
        result.put("payTypeList", payTypeList);
        result.put("tableInfo", tableInfo);
        result.put("payFirst", payFirst);
        result.put("peopleNum", peopleNum);
        // 指定的门店是否不可用(前端据此提示用户已切换到其他门店)
        result.put("storeUnavailable", storeUnavailable);

        return getSuccessResult(result);
    }

    /**
     * 获取微信JSSDK配置（用于微信公众号内H5扫码等功能）
     */
    @ApiOperation(value = "获取微信JSSDK配置")
    @RequestMapping(value = "/jsSdkConfig", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject jsSdkConfig(HttpServletRequest request) {
        String merchantNo = request.getHeader("merchantNo") == null ? "" : request.getHeader("merchantNo");
        Integer merchantId = merchantService.getMerchantId(merchantNo);
        String url = request.getParameter("url");
        if (StringUtil.isEmpty(url)) {
            url = request.getHeader("referer");
        }
        // 去掉URL中的#及其后面部分
        if (url != null && url.contains("#")) {
            url = url.substring(0, url.indexOf("#"));
        }
        Map<String, String> config = weixinService.getJsSdkConfig(merchantId, url);
        return getSuccessResult(config);
    }
}
