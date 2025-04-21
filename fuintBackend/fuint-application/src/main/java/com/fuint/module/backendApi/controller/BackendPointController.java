package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.PointDto;
import com.fuint.common.enums.PointSettingEnum;
import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.PointService;
import com.fuint.common.service.SettingService;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtPoint;
import com.fuint.repository.model.MtSetting;
import com.fuint.repository.model.MtUser;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分管理controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-积分相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/point")
public class BackendPointController extends BaseController {

    /**
     * 配置服务接口
     * */
    private SettingService settingService;

    /**
     * 积分服务接口
     * */
    private PointService pointService;

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 积分明细列表查询
     *
     * @param request HttpServletRequest对象
     * @return 积分明细列表
     */
    @ApiOperation(value = "积分明细列表查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('point:list')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String mobile = request.getParameter("mobile") == null ? "" : request.getParameter("mobile");
        String userId = request.getParameter("userId") == null ? "" : request.getParameter("userId");
        String userNo = request.getParameter("userNo") == null ? "" : request.getParameter("userNo");
        String status = request.getParameter("status") == null ? StatusEnum.ENABLED.getKey() : request.getParameter("status");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);

        Map<String, Object> searchParams = new HashMap<>();
        if (StringUtil.isNotEmpty(mobile)) {
            MtUser userInfo = memberService.queryMemberByMobile(accountInfo.getMerchantId(), mobile);
            if (userInfo != null) {
                searchParams.put("userId", userInfo.getId());
            }
        }
        if (StringUtil.isNotEmpty(userId)) {
            searchParams.put("userId", userId);
        }
        if (StringUtil.isNotEmpty(userNo)) {
            searchParams.put("userNo", userNo);
        }
        if (StringUtil.isNotEmpty(status)) {
            searchParams.put("status", status);
        }
        Integer merchantId = accountInfo.getMerchantId();
        if (merchantId != null && merchantId > 0) {
            searchParams.put("merchantId", merchantId);
        }
        Integer storeId = accountInfo.getStoreId();
        if (storeId != null && storeId > 0) {
            searchParams.put("storeId", storeId);
        }
        paginationRequest.setSearchParams(searchParams);
        PaginationResponse<PointDto> paginationResponse = pointService.queryPointListByPagination(paginationRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);

        return getSuccessResult(result);
    }

    /**
     * 积分设置详情
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "积分设置详情")
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('point:setting')")
    public ResponseObject setting(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        List<MtSetting> settingList = settingService.getSettingList(accountInfo.getMerchantId(), SettingTypeEnum.POINT.getKey());
        Map<String, Object> result = new HashMap();
        String pointNeedConsume = "";
        String canUsedAsMoney = "";
        String exchangeNeedPoint = "";
        String rechargePointSpeed = "";
        String status = "";

        for (MtSetting setting : settingList) {
            if (setting.getName().equals(PointSettingEnum.POINT_NEED_CONSUME.getKey())) {
                pointNeedConsume = setting.getValue();
            } else if (setting.getName().equals(PointSettingEnum.CAN_USE_AS_MONEY.getKey())) {
                canUsedAsMoney = setting.getValue();
            } else if (setting.getName().equals(PointSettingEnum.EXCHANGE_NEED_POINT.getKey())) {
                exchangeNeedPoint = setting.getValue();
            } else if (setting.getName().equals(PointSettingEnum.RECHARGE_POINT_SPEED.getKey())) {
                rechargePointSpeed = setting.getValue();
            }
            status = setting.getStatus();
        }

        result.put("pointNeedConsume", pointNeedConsume);
        result.put("canUsedAsMoney", canUsedAsMoney);
        result.put("exchangeNeedPoint", exchangeNeedPoint);
        result.put("rechargePointSpeed", rechargePointSpeed);
        result.put("status", status);

        return getSuccessResult(result);
    }

    /**
     * 提交积分设置
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "提交积分设置")
    @RequestMapping(value = "/saveSetting", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('point:setting')")
    public ResponseObject saveSetting(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String pointNeedConsume = param.get("pointNeedConsume") != null ? param.get("pointNeedConsume").toString() : "1";
        String canUsedAsMoney = param.get("canUsedAsMoney") != null ? param.get("canUsedAsMoney").toString() : YesOrNoEnum.FALSE.getKey();
        String exchangeNeedPoint = param.get("exchangeNeedPoint") != null ? param.get("exchangeNeedPoint").toString() : "0";
        String rechargePointSpeed = param.get("rechargePointSpeed") != null ? param.get("rechargePointSpeed").toString() : "1";
        String status = request.getParameter("status");

        if (!CommonUtil.isNumeric(pointNeedConsume) || !CommonUtil.isNumeric(exchangeNeedPoint) || !CommonUtil.isNumeric(rechargePointSpeed)) {
            return getFailureResult(201, "输入参数有误");
        }

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo.getMerchantId() == null || accountInfo.getMerchantId() <= 0) {
            return getFailureResult(201, "平台方帐号无法执行该操作，请使用商户帐号操作");
        }

        String operator = accountInfo.getAccountName();

        PointSettingEnum[] settingList = PointSettingEnum.values();
        for (PointSettingEnum setting : settingList) {
            MtSetting info = new MtSetting();
            info.setMerchantId(accountInfo.getMerchantId());
            info.setStoreId(0);
            info.setType(SettingTypeEnum.POINT.getKey());
            info.setName(setting.getKey());

            if (setting.getKey().equals(PointSettingEnum.POINT_NEED_CONSUME.getKey())) {
                info.setValue(pointNeedConsume);
            } else if (setting.getKey().equals(PointSettingEnum.CAN_USE_AS_MONEY.getKey())) {
                info.setValue(canUsedAsMoney);
            } else if (setting.getKey().equals(PointSettingEnum.EXCHANGE_NEED_POINT.getKey())) {
                info.setValue(exchangeNeedPoint);
            } else if (setting.getKey().equals(PointSettingEnum.RECHARGE_POINT_SPEED.getKey())) {
                info.setValue(rechargePointSpeed);
            }

            info.setDescription(setting.getValue());
            info.setStatus(status);
            info.setOperator(operator);
            info.setUpdateTime(new Date());

            settingService.saveSetting(info);
        }

        return getSuccessResult(true);
    }

    /**
     * 提交积分充值
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "提交积分充值")
    @RequestMapping(value = "/doRecharge", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('point:modify')")
    public ResponseObject doRecharge(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String amount = param.get("amount") == null ? "0" : param.get("amount").toString();
        String remark = param.get("remark") == null ? "后台充值" : param.get("remark").toString();
        Integer userId = param.get("userId") == null ? 0 : Integer.parseInt(param.get("userId").toString());
        Integer type = param.get("type") == null ? 1 : Integer.parseInt(param.get("type").toString());

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (!CommonUtil.isNumeric(amount)) {
            return getFailureResult(201, "充值积分必须是数字");
        }

        if (userId < 1) {
            return getFailureResult(201, "充值会员信息不能为空");
        }

        MtPoint mtPoint = new MtPoint();
        if (type == 2) {
            // 扣减积分
            MtUser userInfo = memberService.queryMemberById(userId);
            if (userInfo.getPoint() < (Integer.parseInt(amount))) {
                return getFailureResult(201, "操作失败，积分余额不足");
            }
            mtPoint.setAmount(Integer.parseInt(amount) - (Integer.parseInt(amount)) * 2);
        } else {
            mtPoint.setAmount(Integer.parseInt(amount));
        }
        mtPoint.setMerchantId(accountInfo.getMerchantId());
        mtPoint.setDescription(remark);
        mtPoint.setUserId(userId);
        mtPoint.setOperator(accountInfo.getAccountName());
        mtPoint.setOrderSn("");

        pointService.addPoint(mtPoint);

        return getSuccessResult(true);
    }
}
