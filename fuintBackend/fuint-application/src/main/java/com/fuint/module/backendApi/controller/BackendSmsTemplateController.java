package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.SmsTemplateDto;
import com.fuint.common.service.SmsTemplateService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtSmsTemplate;
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
 * 短信模板管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-短信模板相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/smsTemplate")
public class BackendSmsTemplateController extends BaseController {

    /**
     * 短信模板服务接口
     */
    private SmsTemplateService smsTemplateService;

    /**
     * 查询短信模板列表
     *
     * @param request
     * @return
     * @throws BusinessCheckException
     */
    @ApiOperation(value = "查询短信模板列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('smsTemplate:index')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String name = request.getParameter("content") == null ? "" : request.getParameter("content");
        String code = request.getParameter("code") == null ? "" : request.getParameter("code");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        Map<String, Object> searchParams = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            searchParams.put("merchantId", accountInfo.getMerchantId());
        }
        if (StringUtil.isNotEmpty(code)) {
            searchParams.put("code", code);
        }
        if (StringUtil.isNotEmpty(name)) {
            searchParams.put("name", name);
        }
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);
        paginationRequest.setSearchParams(searchParams);
        PaginationResponse<MtSmsTemplate> paginationResponse = smsTemplateService.querySmsTemplateListByPagination(paginationRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);

        return getSuccessResult(result);
    }

    /**
     * 保存短信模板
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存短信模板")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('smsTemplate:edit')")
    public ResponseObject saveHandler(HttpServletRequest request, @RequestBody SmsTemplateDto smsTemplateDto) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        smsTemplateDto.setMerchantId(accountInfo.getMerchantId());
        smsTemplateService.saveSmsTemplate(smsTemplateDto);
        return getSuccessResult(true);
    }

    /**
     * 模板详情
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "模板详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('smsTemplate:index')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Long id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        MtSmsTemplate mtSmsTemplate = smsTemplateService.querySmsTemplateById(id.intValue());
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            if (!accountInfo.getMerchantId().equals(mtSmsTemplate.getMerchantId())) {
                return getFailureResult(1004);
            }
        }

        Map<String, Object> result = new HashMap();
        result.put("smsTemplate", mtSmsTemplate);

        return getSuccessResult(result);
    }

    /**
     * 删除短信模板
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "删除短信模板")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('smsTemplate:edit')")
    public ResponseObject delete(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        String operator = accountInfo.getAccountName();
        smsTemplateService.deleteTemplate(id, operator);

        return getSuccessResult(true);
    }
}
