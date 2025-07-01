package com.fuint.module.backendApi.controller;

import com.fuint.common.dto.AccountInfo;
import com.fuint.common.service.SettingService;
import com.fuint.common.service.StoreService;
import com.fuint.common.service.TableService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.common.Constants;
import com.fuint.common.enums.StatusEnum;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtStore;
import com.fuint.repository.model.MtTable;
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
 * 桌码管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-桌码相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/table")
public class BackendTableController extends BaseController {

    /**
     * 桌码服务接口
     */
    private TableService tableService;

    /**
     * 系统设置服务接口
     * */
    private SettingService settingService;

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 桌码列表查询
     *
     * @param  request HttpServletRequest对象
     * @return 桌码列表
     */
    @ApiOperation(value = "桌码列表查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('table:index')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String code = request.getParameter("code");
        String status = request.getParameter("status");
        String searchStoreId = request.getParameter("storeId");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        Integer storeId = accountInfo.getStoreId();

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("merchantId", accountInfo.getMerchantId());
        }
        if (StringUtil.isNotEmpty(code)) {
            params.put("code", code);
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
        paginationRequest.setSearchParams(params);
        PaginationResponse<MtTable> paginationResponse = tableService.queryTableListByPagination(paginationRequest);

        List<MtStore> storeList = storeService.getActiveStoreList(accountInfo.getMerchantId(), accountInfo.getStoreId(), null);

        Map<String, Object> result = new HashMap<>();
        result.put("imagePath", settingService.getUploadBasePath());
        result.put("storeList", storeList);
        result.put("paginationResponse", paginationResponse);

        return getSuccessResult(result);
    }

    /**
     * 更新桌码状态
     *
     * @return
     */
    @ApiOperation(value = "更新桌码状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('table:index')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String status = params.get("status") != null ? params.get("status").toString() : StatusEnum.ENABLED.getKey();
        Integer id = params.get("id") == null ? 0 : Integer.parseInt(params.get("id").toString());

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        MtTable mtTable = tableService.queryTableById(id);
        if (mtTable == null) {
            return getFailureResult(201);
        }

        mtTable.setOperator(accountInfo.getAccountName());
        mtTable.setStatus(status);
        tableService.updateTable(mtTable);

        return getSuccessResult(true);
    }

    /**
     * 保存桌码
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存桌码")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('table:index')")
    public ResponseObject saveHandler(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String id = params.get("id") == null ? "" : params.get("id").toString();
        String status = params.get("status") == null ? "" : params.get("status").toString();
        String storeId = params.get("storeId") == null ? "0" : params.get("storeId").toString();
        String sort = params.get("sort") == null ? "0" : params.get("sort").toString();
        String code = params.get("code") == null ? "" : params.get("code").toString();
        String description = params.get("description") == null ? "" : params.get("description").toString();
        String maxPeople = params.get("maxPeople") == null ? "0" : params.get("maxPeople").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        MtTable mtTable = new MtTable();
        mtTable.setOperator(accountInfo.getAccountName());
        mtTable.setStatus(status);
        mtTable.setStoreId(Integer.parseInt(storeId));
        mtTable.setSort(Integer.parseInt(sort));
        mtTable.setMerchantId(accountInfo.getMerchantId());
        mtTable.setCode(code);
        mtTable.setDescription(description);
        mtTable.setMaxPeople(Integer.parseInt(maxPeople));
        if (accountInfo.getMerchantId() == null || accountInfo.getMerchantId() < 1) {
            return getFailureResult(201, "平台方账户无操作权限");
        }
        if (mtTable.getStoreId() == null || mtTable.getStoreId() < 1) {
            return getFailureResult(201, "所属店铺不能为空");
        }
        if (StringUtil.isNotEmpty(id)) {
            mtTable.setId(Integer.parseInt(id));
            tableService.updateTable(mtTable);
        } else {
            tableService.addTable(mtTable);
        }

        return getSuccessResult(true);
    }

    /**
     * 获取桌码详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "获取桌码详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('table:index')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        MtTable tableInfo = tableService.queryTableById(id);
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            if (!accountInfo.getMerchantId().equals(tableInfo.getMerchantId())) {
                return getFailureResult(1004);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tableInfo", tableInfo);

        return getSuccessResult(result);
    }
}
