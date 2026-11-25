package com.fuint.module.backendApi.controller.commission;

import com.fuint.common.Constants;
import com.fuint.common.dto.system.AccountInfo;
import com.fuint.common.dto.commission.CommissionCashDto;
import com.fuint.common.dto.common.ParamDto;
import com.fuint.common.enums.CommissionCashStatusEnum;
import com.fuint.common.enums.CommissionPayTypeEnum;
import com.fuint.common.param.CommissionCashPage;
import com.fuint.common.service.CommissionCashService;
import com.fuint.common.service.StoreService;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.ExcelUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.common.vo.CommissionCashExportVo;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.module.backendApi.request.CommissionCashRequest;
import com.fuint.module.backendApi.request.CommissionSettleConfirmRequest;
import com.fuint.repository.model.MtStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分销提成提现管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Tag(name="管理端-分销提成提现相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/commissionCash")
public class BackendCommissionCashController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BackendCommissionCashController.class);

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 分销提成提现业务接口
     */
    private CommissionCashService commissionCashService;

    /**
     * 分销提成提现记录列表
     */
    @Operation(summary="分销提成提现记录列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public ResponseObject list(@ModelAttribute CommissionCashPage commissionCashPage) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            commissionCashPage.setMerchantId(accountInfo.getMerchantId());
        }
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            commissionCashPage.setStoreId(accountInfo.getStoreId());
        }
        PaginationResponse<CommissionCashDto> paginationResponse = commissionCashService.queryCommissionCashByPagination(commissionCashPage);

        List<MtStore> storeList = storeService.getActiveStoreList(accountInfo.getMerchantId(), accountInfo.getStoreId(), null);

        // 状态列表
        List<ParamDto> statusList = CommissionCashStatusEnum.getCommissionCashStatusList();

        Map<String, Object> result = new HashMap<>();
        result.put("dataList", paginationResponse);
        result.put("storeList", storeList);
        result.put("statusList", statusList);

        return getSuccessResult(result);
    }

    /**
     * 导出结算记录
     */
    @Operation(summary="导出结算记录")
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public void export(HttpServletRequest request, HttpServletResponse response, @ModelAttribute CommissionCashPage commissionCashPage) throws IOException, BusinessCheckException {
        // blob下载时token在请求头，window.open下载时在URL参数，两种都兼容
        String accessToken = request.getHeader("Access-Token");
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = request.getParameter("token");
        }
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(accessToken);
        if (accountInfo == null) {
            throw new BusinessCheckException("登录已过期，请重新登录后再导出");
        }
        commissionCashPage.setPage(Constants.PAGE_NUMBER);
        commissionCashPage.setPageSize(Constants.MAX_ROWS);
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            commissionCashPage.setMerchantId(accountInfo.getMerchantId());
        }
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            commissionCashPage.setStoreId(accountInfo.getStoreId());
        }
        PaginationResponse<CommissionCashDto> paginationResponse = commissionCashService.queryCommissionCashByPagination(commissionCashPage);
        logger.info("导出结算记录，条件：merchantId={}, storeId={}, userType={}, payType={}, status={}, realName={}, mobile={}, 结果条数={}",
                commissionCashPage.getMerchantId(), commissionCashPage.getStoreId(), commissionCashPage.getUserType(),
                commissionCashPage.getPayType(), commissionCashPage.getStatus(), commissionCashPage.getRealName(),
                commissionCashPage.getMobile(), paginationResponse.getContent() == null ? 0 : paginationResponse.getContent().size());

        // 组装导出数据（表头由 CommissionCashExportVo 的 @ExcelProperty 声明）
        List<CommissionCashExportVo> dataList = new ArrayList<>();
        for (CommissionCashDto dto : paginationResponse.getContent()) {
            if (dto == null) {
                continue;
            }
            CommissionCashExportVo vo = new CommissionCashExportVo();
            boolean isMember = dto.getUserId() != null && dto.getUserId() > 0;
            vo.setSettleNo(dto.getSettleNo() == null ? "" : dto.getSettleNo());
            vo.setObjectType(isMember ? "会员" : "员工");
            vo.setUserName(isMember
                    ? (dto.getUserInfo() != null && dto.getUserInfo().getName() != null ? dto.getUserInfo().getName() : "")
                    : (dto.getStaffInfo() != null && dto.getStaffInfo().getRealName() != null ? dto.getStaffInfo().getRealName() : ""));
            vo.setMobile(isMember
                    ? (dto.getUserInfo() != null && dto.getUserInfo().getMobile() != null ? dto.getUserInfo().getMobile() : "")
                    : (dto.getStaffInfo() != null && dto.getStaffInfo().getMobile() != null ? dto.getStaffInfo().getMobile() : ""));
            vo.setStoreName(dto.getStoreInfo() != null && dto.getStoreInfo().getName() != null ? dto.getStoreInfo().getName() : "");
            vo.setAmount(dto.getAmount() != null ? dto.getAmount().toPlainString() : "0");
            String statusText = "";
            for (CommissionCashStatusEnum statusEnum : CommissionCashStatusEnum.values()) {
                if (statusEnum.getKey().equals(dto.getStatus())) {
                    statusText = statusEnum.getValue();
                    break;
                }
            }
            vo.setStatusText(statusText);
            vo.setPayTypeText(CommissionPayTypeEnum.WECHAT.getKey().equals(dto.getPayType()) ? "微信钱包"
                    : (CommissionPayTypeEnum.BALANCE.getKey().equals(dto.getPayType()) ? "会员余额" : "-"));
            vo.setCreateTime(dto.getCreateTime() != null ? DateUtil.formatDate(dto.getCreateTime(), "yyyy-MM-dd HH:mm:ss") : "");
            vo.setPayTime(dto.getPayTime() != null ? DateUtil.formatDate(dto.getPayTime(), "yyyy-MM-dd HH:mm:ss") : "");
            vo.setOperator(dto.getOperator() == null ? "" : dto.getOperator());
            vo.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
            dataList.add(vo);
        }
        ExcelUtil.exportXlsx(response, "结算记录" + DateUtil.formatDate(new Date(), "yyyy.MM.dd_HHmm"), "结算记录", CommissionCashExportVo.class, dataList);
    }

    /**
     * 获取分销提成记录详情
     */
    @Operation(summary="获取分销提成提现详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public ResponseObject info(@PathVariable("id") Integer id) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        CommissionCashDto commissionCash = commissionCashService.queryCommissionCashById(id);
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            if (!accountInfo.getMerchantId().equals(commissionCash.getMerchantId())) {
                return getFailureResult(1004);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("commissionCash", commissionCash);
        return getSuccessResult(result);
    }

    /**
     * 修改分销提成提现
     */
    @Operation(summary="修改分销提成提现")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public ResponseObject save(@RequestBody CommissionCashRequest commissionCashRequest) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        commissionCashRequest.setOperator(accountInfo.getAccountName());
        if (!checkOwner(commissionCashRequest.getId(), accountInfo)) {
            return getFailureResult(1004);
        }
        commissionCashService.updateCommissionCash(commissionCashRequest);
        return getSuccessResult(true);
    }

    /**
     * 支付结算金额到用户余额
     */
    @Operation(summary="付款到会员余额")
    @RequestMapping(value = "/payToBalance", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public ResponseObject payToBalance(@RequestBody CommissionCashRequest commissionCashRequest) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        commissionCashRequest.setOperator(accountInfo.getAccountName());
        if (!checkOwner(commissionCashRequest.getId(), accountInfo)) {
            return getFailureResult(1004);
        }
        commissionCashService.payToBalance(commissionCashRequest);
        return getSuccessResult(true);
    }

    /**
     * 商家转账付款到微信钱包
     */
    @Operation(summary="付款到微信钱包")
    @RequestMapping(value = "/payToWechat", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public ResponseObject payToWechat(@RequestBody CommissionCashRequest commissionCashRequest) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        commissionCashRequest.setOperator(accountInfo.getAccountName());
        if (!checkOwner(commissionCashRequest.getId(), accountInfo)) {
            return getFailureResult(1004);
        }
        commissionCashService.payToWechat(commissionCashRequest);
        return getSuccessResult(true);
    }

    /**
     * 结算确认
     */
    @Operation(summary="结算确认")
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public ResponseObject confirm(@RequestBody CommissionSettleConfirmRequest requestParam) throws BusinessCheckException {
        AccountInfo AccountInfo = TokenUtil.getAccountInfo();
        requestParam.setOperator(AccountInfo.getAccountName());
        if (AccountInfo.getMerchantId() != null && AccountInfo.getMerchantId() > 0) {
            requestParam.setMerchantId(AccountInfo.getMerchantId());
        }
        commissionCashService.confirmCommissionCash(requestParam);
        return getSuccessResult(true);
    }

    /**
     * 取消结算
     */
    @Operation(summary="取消结算")
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('commission:cash:index')")
    public ResponseObject cancel(@RequestBody CommissionSettleConfirmRequest requestParam) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            requestParam.setMerchantId(accountInfo.getMerchantId());
        }
        requestParam.setOperator(accountInfo.getAccountName());
        commissionCashService.cancelCommissionCash(requestParam);
        return getSuccessResult(true);
    }

    private boolean checkOwner(Integer commissionCashId, AccountInfo accountInfo) throws BusinessCheckException {
        CommissionCashDto commissionCash = commissionCashService.queryCommissionCashById(commissionCashId);
        if (commissionCash == null) {
            return false;
        }
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            if (!accountInfo.getMerchantId().equals(commissionCash.getMerchantId())) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
