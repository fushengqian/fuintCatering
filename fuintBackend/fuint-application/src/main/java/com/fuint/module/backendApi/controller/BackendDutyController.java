package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountDto;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.RoleDto;
import com.fuint.common.enums.AdminRoleEnum;
import com.fuint.common.service.DutyService;
import com.fuint.common.service.SourceService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.module.backendApi.request.DutyStatusRequest;
import com.fuint.repository.model.TDuty;
import com.fuint.repository.model.TSource;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台角色管理控制类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-后台角色相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/duty")
public class BackendDutyController extends BaseController {

    /**
     * 角色服务接口
     * */
    private DutyService tDutyService;

    /**
     * 菜单服务接口
     * */
    private SourceService tSourceService;

    /**
     * 角色列表
     *
     * @param request HttpServletRequest对象
     * @return 角色信息列表
     */
    @ApiOperation(value = "获取角色列表")
    @RequestMapping(value = "/list")
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:role:index')")
    public ResponseObject list(HttpServletRequest request) {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String name = request.getParameter("name") == null ? "" : request.getParameter("name");
        String status = request.getParameter("status") == null ? "" : request.getParameter("status");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> searchParams = new HashMap<>();
        if (StringUtil.isNotEmpty(name)) {
            searchParams.put("name", name);
        }
        if (StringUtil.isNotEmpty(status)) {
            searchParams.put("status", status);
        }
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            searchParams.put("merchantId", accountInfo.getMerchantId());
        }

        paginationRequest.setSearchParams(searchParams);
        PaginationResponse<TDuty> paginationResponse = tDutyService.findDutiesByPagination(paginationRequest);
        List<RoleDto> content = new ArrayList<>();
        if (paginationResponse.getContent().size() > 0) {
            for (TDuty tDuty : paginationResponse.getContent()) {
                 RoleDto dto = new RoleDto();
                 dto.setId(tDuty.getDutyId().longValue());
                 dto.setName(tDuty.getDutyName());
                 String type = AdminRoleEnum.getName(tDuty.getDutyType());
                 dto.setType(type);
                 dto.setStatus(tDuty.getStatus());
                 content.add(dto);
            }
        }

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        Page pageImpl = new PageImpl(content, pageRequest, paginationResponse.getTotalElements());
        PaginationResponse<RoleDto> result = new PaginationResponse(pageImpl, AccountDto.class);
        result.setTotalPages(paginationResponse.getTotalPages());
        result.setTotalElements(paginationResponse.getTotalElements());
        result.setContent(content);

        return getSuccessResult(result);
    }

    /**
     * 新增角色
     *
     * @param  request  HttpServletRequest对象
     * @return 角色列表页面
     * @throws BusinessCheckException
     */
    @ApiOperation(value = "新增角色")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:role:add')")
    public ResponseObject addHandler(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        List<Integer> menuIds = (List) param.get("menuIds");
        String name = param.get("roleName").toString();
        String type = param.get("roleType").toString();
        String status = param.get("status").toString();
        String description = param.get("description").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        // 获取角色所分配的菜单
        List<TSource> sources = null;

        if (menuIds.size() > 0) {
            String[] sourceIds = new String[menuIds.size()];
            for (int i = 0; i < sourceIds.length; i++) {
                 sourceIds[i] = menuIds.get(i).toString();
            }
            sources = tSourceService.findDatasByIds(sourceIds);
        }

        TDuty tDuty = new TDuty();
        tDuty.setMerchantId(accountInfo.getMerchantId());
        tDuty.setDutyName(name);
        tDuty.setDutyType(type);
        tDuty.setStatus(status);
        tDuty.setDescription(description);

        // 保存角色
        tDutyService.saveDuty(tDuty, sources);
        return getSuccessResult(true);
    }

    /**
     * 获取角色详情
     *
     * @param  roleId
     * @return 账户信息
     */
    @ApiOperation(value = "获取角色详情")
    @RequestMapping(value = "/info/{roleId}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:role:index')")
    public ResponseObject info(@PathVariable("roleId") Long roleId) {
        TDuty htDuty = tDutyService.getRoleById(roleId);

        Map<String, Object> result = new HashMap<>();

        RoleDto roleInfo = new RoleDto();
        roleInfo.setMerchantId(htDuty.getMerchantId());
        roleInfo.setId(htDuty.getDutyId().longValue());
        roleInfo.setName(htDuty.getDutyName());
        roleInfo.setType(htDuty.getDutyType());
        roleInfo.setStatus(htDuty.getStatus());
        roleInfo.setDescription(htDuty.getDescription());

        result.put("roleInfo", roleInfo);
        List<Long> checkedKeys = tDutyService.getSourceIdsByDutyId(roleId.intValue());
        if (checkedKeys != null && checkedKeys.size() > 0) {
            result.put("checkedKeys", checkedKeys);
        }

        return getSuccessResult(result);
    }

    /**
     * 修改角色
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "修改角色")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:role:edit')")
    public ResponseObject updateHandler(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        List<Integer> menuIds = (List) param.get("menuIds");
        String id = param.get("id").toString();
        String name = param.get("roleName").toString();
        String type = param.get("roleType").toString();
        String status = param.get("status").toString();
        String description = param.get("description").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (StringUtil.isEmpty(id)) {
            return getFailureResult(201, "信息提交有误");
        }

        TDuty duty = tDutyService.getRoleById(Long.parseLong(id));
        if (!duty.getMerchantId().equals(accountInfo.getMerchantId()) && accountInfo.getMerchantId() > 0) {
            return getFailureResult(201, "抱歉，您没有修改权限");
        }

        duty.setDescription(description);
        duty.setDutyName(name);
        duty.setStatus(status);
        duty.setDutyType(type);

        // 获取角色所分配的菜单
        List<TSource> sources = null;
        if (menuIds.size() > 0) {
            String[] sourceIds = new String[menuIds.size()];
            for (int i = 0; i < sourceIds.length; i++) {
                 sourceIds[i] = menuIds.get(i).toString();
            }
            sources = tSourceService.findDatasByIds(sourceIds);
        }

        tDutyService.updateDuty(duty, sources);
        return getSuccessResult(true);
    }

    /**
     * 删除角色信息
     *
     * @return
     * @throws BusinessCheckException
     */
    @ApiOperation(value = "删除角色信息")
    @RequestMapping(value = "/delete/{roleId}", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:role:delete')")
    public ResponseObject deleteRole(HttpServletRequest request, @PathVariable("roleId") Long roleId) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        tDutyService.deleteDuty(accountInfo.getMerchantId(), roleId);
        return getSuccessResult(true);
    }

    /**
     * 修改角色状态
     *
     * @return
     * @throws BusinessCheckException
     */
    @ApiOperation(value = "修改角色状态")
    @RequestMapping(value = "/changeStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('system:role:edit')")
    public ResponseObject changeStatus(HttpServletRequest request, @RequestBody DutyStatusRequest dutyStatusRequest) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        tDutyService.updateStatus(accountInfo.getMerchantId(), dutyStatusRequest);
        return getSuccessResult(true);
    }
}
