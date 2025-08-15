package com.fuint.module.backendApi.controller;

import com.fuint.common.dto.AccountInfo;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.common.Constants;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.TableAreaService;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtTableArea;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 桌码区域管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-桌码区域相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/tableArea")
public class BackendTableAreaController extends BaseController {

    /**
     * 桌码区域服务接口
     */
    private TableAreaService tableAreaService;

    /**
     * 桌码区域列表查询
     */
    @ApiOperation(value = "桌码区域列表查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:list')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String title = request.getParameter("title");
        String status = request.getParameter("status");
        String searchStoreId = request.getParameter("storeId");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));
        Integer storeId = accountInfo.getStoreId();

        Map<String, Object> params = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("merchantId", accountInfo.getMerchantId());
        }
        if (StringUtil.isNotEmpty(title)) {
            params.put("title", title);
        }
        if (StringUtil.isNotEmpty(status)) {
            params.put("status", status);
        }
        if (StringUtil.isNotEmpty(searchStoreId)) {
            params.put("storeId", searchStoreId);
        }
        if (storeId != null && storeId > 0) {
            params.put("storeId", storeId);
        }
        PaginationResponse<MtTableArea> paginationResponse = tableAreaService.queryTableAreaListByPagination(new PaginationRequest(page, pageSize, params));

        Map<String, Object> paramsStore = new HashMap<>();
        paramsStore.put("status", StatusEnum.ENABLED.getKey());
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            paramsStore.put("storeId", accountInfo.getStoreId().toString());
        }
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            paramsStore.put("merchantId", accountInfo.getMerchantId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);

        return getSuccessResult(result);
    }

    /**
     * 更新桌码区域状态
     */
    @ApiOperation(value = "更新桌码区域状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:edit')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String status = params.get("status") != null ? params.get("status").toString() : StatusEnum.ENABLED.getKey();
        Integer id = params.get("id") == null ? 0 : Integer.parseInt(params.get("id").toString());

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));

        MtTableArea mtTableArea = tableAreaService.queryTableAreaById(id);
        if (mtTableArea == null) {
            return getFailureResult(201);
        }

        mtTableArea.setOperator(accountInfo.getAccountName());
        mtTableArea.setStatus(status);
        tableAreaService.updateTableArea(mtTableArea);

        return getSuccessResult(true);
    }

    /**
     * 保存桌码区域
     */
    @ApiOperation(value = "保存桌码区域")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:add')")
    public ResponseObject saveHandler(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String id = params.get("id") == null ? "" : params.get("id").toString();
        String status = params.get("status") == null ? "" : params.get("status").toString();
        String storeId = params.get("storeId") == null ? "0" : params.get("storeId").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));

        MtTableArea info = new MtTableArea();
        info.setOperator(accountInfo.getAccountName());
        info.setStatus(status);
        info.setStoreId(Integer.parseInt(storeId));
        info.setMerchantId(accountInfo.getMerchantId());
        if (StringUtil.isNotEmpty(id)) {
            info.setId(Integer.parseInt(id));
            tableAreaService.updateTableArea(info);
        } else {
            tableAreaService.addTableArea(info);
        }

        return getSuccessResult(true);
    }

    /**
     * 获取桌码区域详情
     */
    @ApiOperation(value = "获取桌码区域详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:list')")
    public ResponseObject info(@PathVariable("id") Integer id) throws BusinessCheckException {
        MtTableArea tableAreaInfo = tableAreaService.queryTableAreaById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("tableAreaInfo", tableAreaInfo);

        return getSuccessResult(result);
    }
}
