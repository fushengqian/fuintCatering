package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.ParamDto;
import com.fuint.common.enums.StaffCategoryEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.StaffService;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.PhoneFormatCheckUtils;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtStaff;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺员工管理
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-店铺员工相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/staff")
public class BackendStaffController extends BaseController {

    /**
     * 员工接口
     */
    private StaffService staffService;

    /**
     * 获取员工列表
     *
     * @param  request HttpServletRequest对象
     * @return 员工列表页面
     */
    @ApiOperation(value = "获取员工列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('staff:list')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String mobile = request.getParameter("mobile");
        String realName = request.getParameter("realName");
        String auditedStatus = request.getParameter("auditedStatus");
        String storeId = request.getParameter("storeId");
        String category = request.getParameter("category");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            storeId = accountInfo.getStoreId().toString();
        }
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> params = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("merchantId", accountInfo.getMerchantId());
        }
        if (StringUtil.isNotEmpty(realName)) {
            params.put("name", realName);
        }
        if (StringUtil.isNotEmpty(mobile)) {
            params.put("mobile", mobile);
        }
        if (StringUtil.isNotEmpty(auditedStatus)) {
            params.put("status", auditedStatus);
        }
        if (StringUtil.isNotEmpty(storeId)) {
            params.put("storeId", storeId);
        }
        if (StringUtil.isNotEmpty(category)) {
            params.put("category", category);
        }
        paginationRequest.setSearchParams(params);
        PaginationResponse<MtStaff> paginationResponse = staffService.queryStaffListByPagination(paginationRequest);

        // 员工类别列表
        StaffCategoryEnum[] categoryListEnum = StaffCategoryEnum.values();
        List<ParamDto> categoryList = new ArrayList<>();
        for (StaffCategoryEnum enumItem : categoryListEnum) {
            ParamDto paramDto = new ParamDto();
            paramDto.setKey(enumItem.getKey());
            paramDto.setName(enumItem.getName());
            paramDto.setValue(enumItem.getKey());
            categoryList.add(paramDto);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);
        result.put("categoryList", categoryList);

        return getSuccessResult(result);
    }

    /**
     * 更新员工状态
     * @return
     */
    @ApiOperation(value = "更新员工状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('staff:list')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String status = params.get("status") != null ? params.get("status").toString() : StatusEnum.ENABLED.getKey();
        Integer id = params.get("id") == null ? 0 : Integer.parseInt(params.get("id").toString());

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        staffService.updateAuditedStatus(id, status, accountInfo.getAccountName());
        return getSuccessResult(true);
    }

    /**
     * 保存员工信息
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "保存员工信息")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('staff:list')")
    public ResponseObject saveHandler(HttpServletRequest request, @RequestBody Map<String, Object> params) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String id = params.get("id") == null ? "0" : params.get("id").toString();
        String storeId = params.get("storeId") == null ? "0" : params.get("storeId").toString();
        String category = params.get("category") == null ? "0" : params.get("category").toString();
        String mobile = params.get("mobile") == null ? "" : CommonUtil.replaceXSS(params.get("mobile").toString());
        String realName = params.get("realName") == null ? "" : CommonUtil.replaceXSS(params.get("realName").toString());
        String description = params.get("description") == null ? "" : CommonUtil.replaceXSS(params.get("description").toString());
        String status = params.get("auditedStatus") == null ? StatusEnum.FORBIDDEN.getKey() : CommonUtil.replaceXSS(params.get("auditedStatus").toString());

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (accountInfo.getMerchantId() == null || accountInfo.getMerchantId() <= 0) {
            return getFailureResult(201, "平台方帐号无法执行该操作，请使用商户帐号操作");
        }

        MtStaff mtStaff = new MtStaff();
        if (StringUtil.isNotEmpty(id)) {
            mtStaff = staffService.queryStaffById(Integer.parseInt(id));
        }

        if (mtStaff == null && StringUtil.isNotEmpty(id)) {
            return getFailureResult(201, "员工信息不存在");
        }
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            mtStaff.setMerchantId(accountInfo.getMerchantId());
        }
        mtStaff.setStoreId(Integer.parseInt(storeId));
        mtStaff.setRealName(realName);
        if (PhoneFormatCheckUtils.isChinaPhoneLegal(mobile)) {
            mtStaff.setMobile(mobile);
        }
        mtStaff.setAuditedStatus(status);
        mtStaff.setDescription(description);
        mtStaff.setCategory(Integer.parseInt(category));

        if (StringUtil.isEmpty(mtStaff.getMobile())) {
            return getFailureResult(201, "手机号码不能为空");
        } else {
            MtStaff tempUser = staffService.queryStaffByMobile(mtStaff.getMobile());
            if (tempUser != null && !tempUser.getId().equals(mtStaff.getId())) {
                return getFailureResult(201, "该手机号码已经存在");
            }
        }
        staffService.saveStaff(mtStaff, accountInfo.getAccountName());
        return getSuccessResult(true);
    }

    /**
     * 查询员工详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "查询员工详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('staff:list')")
    public ResponseObject getStaffInfo(@PathVariable("id") Integer id) throws BusinessCheckException {
        MtStaff staffInfo = staffService.queryStaffById(id);
        if (staffInfo != null) {
            staffInfo.setMobile(CommonUtil.hidePhone(staffInfo.getMobile()));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("staffInfo", staffInfo);

        return getSuccessResult(result);
    }

    /**
     * 店铺员工列表
     *
     * @param storeId
     * @return
     */
    @ApiOperation(value = "店铺员工列表")
    @RequestMapping(value = "/storeStaffList/{storeId}", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject storeStaffList(HttpServletRequest request, @PathVariable("storeId") Integer storeId) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        Map<String, Object> params = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("MERCHANT_ID", accountInfo.getMerchantId());
        }
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            storeId = accountInfo.getStoreId();
        }
        params.put("AUDITED_STATUS", StatusEnum.ENABLED.getKey());
        if (storeId != null && storeId > 0) {
            params.put("STORE_ID", storeId);
        }
        List<MtStaff> staffList = staffService.queryStaffByParams(params);

        Map<String, Object> result = new HashMap<>();
        result.put("staffList", staffList);

        return getSuccessResult(result);
    }

    /**
     * 删除员工
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "删除员工")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('staff:list')")
    public ResponseObject deleteStaff(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        staffService.updateAuditedStatus(id, StatusEnum.DISABLE.getKey(), accountInfo.getAccountName());
        return getSuccessResult(true);
    }
}
