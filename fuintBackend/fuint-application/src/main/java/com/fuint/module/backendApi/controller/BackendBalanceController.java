package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.BalanceDto;
import com.fuint.common.dto.RechargeRuleDto;
import com.fuint.common.enums.BalanceSettingEnum;
import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.BalanceService;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.SettingService;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtBalance;
import com.fuint.repository.model.MtSetting;
import com.fuint.repository.model.MtUser;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * 余额管理controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-余额相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/balance")
public class BackendBalanceController extends BaseController {

    /**
     * 配置服务接口
     * */
    private SettingService settingService;

    /**
     * 余额服务接口
     * */
    private BalanceService balanceService;

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 余额明细列表查询
     *
     * @param request HttpServletRequest对象
     * @return 余额明细列表
     */
    @ApiOperation(value = "余额明细列表查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('balance:list')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String mobile = request.getParameter("mobile") == null ? "" : request.getParameter("mobile");
        String userId = request.getParameter("userId") == null ? "" : request.getParameter("userId");
        String userNo = request.getParameter("userNo") == null ? "" : request.getParameter("userNo");
        String orderSn = request.getParameter("orderSn") == null ? "" : request.getParameter("orderSn");
        String status = request.getParameter("status") == null ? StatusEnum.ENABLED.getKey() : request.getParameter("status");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        Map<String, Object> searchParams = new HashMap<>();
        if (StringUtil.isNotEmpty(mobile)) {
            searchParams.put("mobile", mobile);
        }
        if (StringUtil.isNotEmpty(userId)) {
            searchParams.put("userId", userId);
        }
        if (StringUtil.isNotEmpty(userNo)) {
            searchParams.put("userNo", userNo);
        }
        if (StringUtil.isNotEmpty(orderSn)) {
            searchParams.put("orderSn", orderSn);
        }
        if (StringUtil.isNotEmpty(status)) {
            searchParams.put("status", status);
        }
        Integer storeId = accountInfo.getStoreId();
        if (storeId != null && storeId > 0) {
            searchParams.put("storeId", storeId);
        }
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            searchParams.put("merchantId", accountInfo.getMerchantId());
        }

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);
        paginationRequest.setSearchParams(searchParams);

        PaginationResponse<BalanceDto> paginationResponse = balanceService.queryBalanceListByPagination(paginationRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);
        return getSuccessResult(result);
    }

    /**
     * 提交充值（单个会员）
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "提交充值")
    @RequestMapping(value = "/doRecharge", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('balance:modify')")
    public ResponseObject doRecharge(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String amount = param.get("amount") == null ? "0" : param.get("amount").toString();
        String remark = param.get("remark") == null ? "后台充值" : param.get("remark").toString();
        Integer userId = param.get("userId") == null ? 0 : Integer.parseInt(param.get("userId").toString());
        Integer type = param.get("type") == null ? 1 : Integer.parseInt(param.get("type").toString());// 1 增加，2 扣减

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (!CommonUtil.isNumeric(amount)) {
            return getFailureResult(201, "充值金额必须是数字");
        }
        if (userId < 1) {
            return getFailureResult(201, "充值会员信息不能为空");
        }

        String operator = accountInfo.getAccountName();
        MtBalance mtBalance = new MtBalance();

        MtUser userInfo = memberService.queryMemberById(userId);

        // 扣减余额
        if (type == 2) {
            if (userInfo.getBalance().compareTo(new BigDecimal(amount)) < 0) {
                return getFailureResult(201, "操作失败，会员余额不足");
            }
            mtBalance.setAmount(new BigDecimal(amount).subtract(new BigDecimal(amount).multiply(new BigDecimal("2"))));
        } else {
            mtBalance.setAmount(new BigDecimal(amount));
        }
        mtBalance.setMerchantId(accountInfo.getMerchantId());
        mtBalance.setDescription(remark);
        mtBalance.setUserId(userId);
        mtBalance.setOperator(operator);
        mtBalance.setOrderSn("");

        balanceService.addBalance(mtBalance, true);
        return getSuccessResult(true);
    }

    /**
     * 发放余额
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "发放余额")
    @RequestMapping(value = "/distribute", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('balance:distribute')")
    public ResponseObject distribute(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String amount = param.get("amount") == null ? "0" : param.get("amount").toString();
        String remark = param.get("remark") == null ? "后台充值" : param.get("remark").toString();
        String userIds = param.get("userIds") == null ? "" : param.get("userIds").toString();
        String object = param.get("object") == null ? "" : param.get("object").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        balanceService.distribute(accountInfo, object, userIds, amount, remark);
        return getSuccessResult(true);
    }

    /**
     * 充值设置详情
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "充值设置详情")
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('balance:setting')")
    public ResponseObject setting(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        List<MtSetting> settingList = settingService.getSettingList(accountInfo.getMerchantId(), SettingTypeEnum.BALANCE.getKey());

        List<RechargeRuleDto> rechargeRuleList = new ArrayList<>();
        String remark = "";
        String status = "";
        if (settingList.size() > 0) {
            for (MtSetting setting : settingList) {
                 if (setting.getName().equals(BalanceSettingEnum.RECHARGE_RULE.getKey())) {
                     status = setting.getStatus();
                     String item[] = setting.getValue().split(",");
                     if (item.length > 0) {
                         for (String value : item) {
                              String el[] = value.split("_");
                              if (el.length == 2) {
                                  RechargeRuleDto e = new RechargeRuleDto();
                                  e.setRechargeAmount(el[0]);
                                  e.setGiveAmount(el[1]);
                                  rechargeRuleList.add(e);
                              }
                         }
                     }
                 } else if(setting.getName().equals(BalanceSettingEnum.RECHARGE_REMARK.getKey())) {
                     remark = setting.getValue();
                 }
            }
        }

        Map<String, Object> result = new HashMap();
        result.put("rechargeRuleList", rechargeRuleList);
        result.put("remark", remark);
        result.put("status", status);

        return getSuccessResult(result);
    }

    /**
     * 保存充值设置
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "保存充值设置")
    @RequestMapping(value = "/saveSetting", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('balance:setting')")
    public ResponseObject saveSetting(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String status = param.get("status") == null ? StatusEnum.ENABLED.getKey() : param.get("status").toString();
        String remark = param.get("remark") == null ? "" : param.get("remark").toString();
        List<LinkedHashMap> rechargeItems = (List) param.get("rechargeItem");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (rechargeItems.size() < 0) {
            return getFailureResult(201, "充值规则设置不能为空");
        }
        if (accountInfo.getMerchantId() == null || accountInfo.getMerchantId() <= 0) {
            return getFailureResult(201, "平台方帐号无法执行该操作，请使用商户帐号操作");
        }

        String rechargeRule = "";
        List<String> amounts = new ArrayList<>();
        for (LinkedHashMap item : rechargeItems) {
             String amount = item.get("rechargeAmount").toString();
             if (amounts.contains(amount)) {
                 return getFailureResult(201, "充值金额设置不能有重复");
             }
             if (rechargeRule.length() == 0) {
                 rechargeRule = item.get("rechargeAmount").toString() + '_' + item.get("giveAmount").toString();
             } else {
                 rechargeRule = rechargeRule + ',' + item.get("rechargeAmount").toString() + '_' + item.get("giveAmount").toString();
             }
             amounts.add(amount);
        }

        MtSetting setting = new MtSetting();
        setting.setMerchantId(accountInfo.getMerchantId());
        setting.setType(SettingTypeEnum.BALANCE.getKey());
        setting.setName(BalanceSettingEnum.RECHARGE_RULE.getKey());
        setting.setValue(rechargeRule);
        setting.setDescription(BalanceSettingEnum.RECHARGE_RULE.getValue());
        setting.setStatus(status);
        setting.setOperator(accountInfo.getAccountName());
        setting.setUpdateTime(new Date());
        settingService.saveSetting(setting);

        // 保存充值说明
        MtSetting settingRemark = new MtSetting();
        settingRemark.setMerchantId(accountInfo.getMerchantId());
        settingRemark.setType(SettingTypeEnum.BALANCE.getKey());
        settingRemark.setName(BalanceSettingEnum.RECHARGE_REMARK.getKey());
        settingRemark.setValue(remark);
        settingRemark.setDescription("");
        settingRemark.setStatus(status);
        settingRemark.setOperator(accountInfo.getAccountName());
        settingRemark.setUpdateTime(new Date());
        settingService.saveSetting(settingRemark);

        return getSuccessResult(true);
    }
}
