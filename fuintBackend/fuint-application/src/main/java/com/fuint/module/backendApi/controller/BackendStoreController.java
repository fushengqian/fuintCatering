package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.StoreDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.MerchantService;
import com.fuint.common.service.SettingService;
import com.fuint.common.service.StoreService;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtMerchant;
import com.fuint.repository.model.MtStore;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-店铺相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/store")
public class BackendStoreController extends BaseController {

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 商户接口
     */
    private MerchantService merchantService;

    /**
     * 系统设置服务接口
     * */
    private SettingService settingService;

    /**
     * 获取店铺列表
     *
     * @param request HttpServletRequest对象
     * @return 店铺列表
     */
    @ApiOperation(value = "获取店铺列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('store:list')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));

        String storeName = request.getParameter("name");
        String storeStatus = request.getParameter("status");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("merchantId", accountInfo.getMerchantId());
        }
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            params.put("storeId", accountInfo.getStoreId());
        }
        if (StringUtil.isNotEmpty(storeName)) {
            params.put("name", storeName);
        }
        if (StringUtil.isNotEmpty(storeStatus)) {
            params.put("status", storeStatus);
        }
        paginationRequest.setSearchParams(params);
        PaginationResponse<StoreDto> paginationResponse = storeService.queryStoreListByPagination(paginationRequest);

        Map<String, Object> param = new HashMap<>();
        param.put("status", StatusEnum.ENABLED.getKey());
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            param.put("merchantId", accountInfo.getMerchantId());
        }
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            param.put("storeId", accountInfo.getStoreId());
        }
        List<MtMerchant> merchantList = merchantService.queryMerchantByParams(param);
        String imagePath = settingService.getUploadBasePath();

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);
        result.put("merchantList", merchantList);
        result.put("imagePath", imagePath);

        return getSuccessResult(result);
    }

    /**
     * 搜索店铺
     * */
    @ApiOperation(value = "搜索店铺")
    @RequestMapping(value = "/searchStore",  method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject search(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String merchantId = request.getParameter("merchantId") == null ? "" : request.getParameter("merchantId");
        String storeId = request.getParameter("id") == null ? "" : request.getParameter("id");
        String storeName = request.getParameter("name") == null ? "" : request.getParameter("name");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            storeId = accountInfo.getStoreId().toString();
        }

        Map<String, Object> paramsStore = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            merchantId = accountInfo.getMerchantId().toString();
        }
        if (StringUtil.isNotEmpty(merchantId)) {
            paramsStore.put("merchantId", merchantId);
        }
        if (StringUtil.isNotEmpty(storeId)) {
            paramsStore.put("storeId", storeId);
        }
        if (StringUtil.isNotEmpty(storeName)) {
            paramsStore.put("name", storeName);
        }

        paramsStore.put("status", StatusEnum.ENABLED.getKey());
        List<MtStore> storeList = storeService.queryStoresByParams(paramsStore);
        Map<String, Object> result = new HashMap<>();
        result.put("storeList", storeList);

        return getSuccessResult(result);
    }

    /**
     * 更新店铺状态
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "更新店铺状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('store:add')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String status = params.get("status") != null ? params.get("status").toString() : StatusEnum.ENABLED.getKey();
        Integer storeId = params.get("storeId") == null ? 0 : Integer.parseInt(params.get("storeId").toString());

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        String operator = accountInfo.getAccountName();
        storeService.updateStatus(storeId, operator, status);

        return getSuccessResult(true);
    }

    /**
     * 保存店铺
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存店铺")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('store:add')")
    public ResponseObject saveHandler(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        StoreDto storeInfo = new StoreDto();
        String storeId = params.get("id").toString();
        String storeName = CommonUtil.replaceXSS(params.get("name").toString());
        String contact = CommonUtil.replaceXSS(params.get("contact").toString());
        String phone = CommonUtil.replaceXSS(params.get("phone").toString());
        String description = params.get("description") == null ? "" : CommonUtil.replaceXSS(params.get("description").toString());
        String isDefault = params.get("isDefault") == null ? YesOrNoEnum.NO.getKey() : CommonUtil.replaceXSS(params.get("isDefault").toString());
        String address = params.get("address") == null ? "" : CommonUtil.replaceXSS(params.get("address").toString());
        String hours = params.get("hours") == null ? "" : CommonUtil.replaceXSS(params.get("hours").toString());
        String latitude = params.get("latitude") == null ? "" : CommonUtil.replaceXSS(params.get("latitude").toString());
        String longitude = params.get("longitude") == null ? "" : CommonUtil.replaceXSS(params.get("longitude").toString());
        String wxMchId = params.get("wxMchId") == null ? null : CommonUtil.replaceXSS(params.get("wxMchId").toString());
        String wxApiV2 = params.get("wxApiV2") == null ? null : CommonUtil.replaceXSS(params.get("wxApiV2").toString());
        String wxCertPath = params.get("wxCertPath") == null ? null : CommonUtil.replaceXSS(params.get("wxCertPath").toString());
        String alipayAppId = params.get("alipayAppId") == null ? null : CommonUtil.replaceXSS(params.get("alipayAppId").toString());
        String alipayPrivateKey = params.get("alipayPrivateKey") == null ? null : CommonUtil.replaceXSS(params.get("alipayPrivateKey").toString());
        String alipayPublicKey = params.get("alipayPublicKey") == null ? null : CommonUtil.replaceXSS(params.get("alipayPublicKey").toString());
        String logo = params.get("logo") == null ? "" : CommonUtil.replaceXSS(params.get("logo").toString());
        String license = params.get("license") == null ? "" : CommonUtil.replaceXSS(params.get("license").toString());
        String creditCode = params.get("creditCode") == null ? "" : CommonUtil.replaceXSS(params.get("creditCode").toString());
        String bankName = params.get("bankName") == null ? "" : CommonUtil.replaceXSS(params.get("bankName").toString());
        String bankCardName = params.get("bankCardName") == null ? "" : CommonUtil.replaceXSS(params.get("bankCardName").toString());
        String bankCardNo = params.get("bankCardNo") == null ? "" : CommonUtil.replaceXSS(params.get("bankCardNo").toString());
        String status = params.get("status") != null ? params.get("status").toString() : StatusEnum.ENABLED.getKey();
        String merchantId = params.get("merchantId").toString();

        if ((StringUtil.isEmpty(latitude) || StringUtil.isEmpty(longitude)) && StringUtil.isNotEmpty(address)) {
            Map<String, Object> latAndLng = CommonUtil.getLatAndLngByAddress(address);
            latitude = latAndLng.get("lat").toString();
            longitude = latAndLng.get("lng").toString();
        }

        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            if (StringUtil.isEmpty(storeId)) {
                return getFailureResult(201, "店铺帐号不能新增店铺，请使用商户帐号添加！");
            }
            storeId = accountInfo.getStoreId().toString();
        }
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            merchantId = accountInfo.getMerchantId().toString();
        }

        storeInfo.setName(storeName);
        storeInfo.setLogo(logo);
        storeInfo.setContact(contact);
        storeInfo.setPhone(phone);
        storeInfo.setDescription(description);
        storeInfo.setIsDefault(isDefault);
        storeInfo.setAddress(address);
        storeInfo.setHours(hours);
        storeInfo.setLatitude(latitude);
        storeInfo.setLongitude(longitude);
        storeInfo.setStatus(status);
        storeInfo.setWxMchId(wxMchId);
        storeInfo.setWxApiV2(wxApiV2);
        storeInfo.setWxCertPath(wxCertPath);
        storeInfo.setLicense(license);
        storeInfo.setCreditCode(creditCode);
        storeInfo.setBankName(bankName);
        storeInfo.setBankCardName(bankCardName);
        storeInfo.setBankCardNo(bankCardNo);
        storeInfo.setAlipayAppId(alipayAppId);
        storeInfo.setAlipayPrivateKey(alipayPrivateKey);
        storeInfo.setAlipayPublicKey(alipayPublicKey);
        if (StringUtil.isNotEmpty(merchantId)) {
            storeInfo.setMerchantId(Integer.parseInt(merchantId));
        }
        if (StringUtil.isEmpty(storeName)) {
            return getFailureResult(201, "店铺名称不能为空");
        } else {
            if (!StringUtil.isNotEmpty(storeName)) {
                StoreDto storeDto = storeService.queryStoreByName(storeName);
                if (null != storeDto && storeDto.getName().equals(storeName) && !storeDto.getId().equals(storeId)) {
                    return getFailureResult(201, "该店铺名称已经存在");
                }
            }
        }

        // 修改店铺
        if (StringUtil.isNotEmpty(storeId)) {
            storeInfo.setId(Integer.parseInt(storeId));
        }

        String operator = accountInfo.getAccountName();
        storeInfo.setOperator(operator);
        storeService.saveStore(storeInfo);

        return getSuccessResult(true);
    }

    /**
     * 获取店铺详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取店铺详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('store:list')")
    public ResponseObject getStoreInfo(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        StoreDto storeInfo = storeService.queryStoreDtoById(id);
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            if (!accountInfo.getMerchantId().equals(storeInfo.getMerchantId())) {
                return getFailureResult(1004);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("storeInfo", storeInfo);

        return getSuccessResult(result);
    }
}
