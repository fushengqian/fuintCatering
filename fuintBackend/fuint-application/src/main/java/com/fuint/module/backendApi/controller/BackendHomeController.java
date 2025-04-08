package com.fuint.module.backendApi.controller;

import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.service.AccountService;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.OrderService;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.TimeUtils;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.TAccount;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-首页相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/home")
public class BackendHomeController extends BaseController {

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 后台账号服务接口
     * */
    private AccountService accountService;

    /**
     * 首页统计数据
     *
     * @return
     */
    @ApiOperation(value = "首页统计数据")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject index(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");

        Date beginTime = DateUtil.getDayBegin();
        Date endTime = DateUtil.getDayEnd();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        Integer merchantId = accountInfo.getMerchantId();
        Integer storeId = accountInfo.getStoreId();

        // 总会员数
        Long totalUser = memberService.getUserCount(merchantId, storeId);
        // 今日新增会员数量
        Long todayUser = memberService.getUserCount(merchantId, storeId, beginTime, endTime);

        // 总订单数
        BigDecimal totalOrder = orderService.getOrderCount(merchantId, storeId);
        // 今日订单数
        BigDecimal todayOrder = orderService.getOrderCount(merchantId, storeId, beginTime, endTime);

        // 今日交易金额
        BigDecimal todayPay = orderService.getPayMoney(merchantId, storeId, beginTime, endTime);
        // 总交易金额
        BigDecimal totalPay = orderService.getPayMoney(merchantId, storeId);

        // 今日活跃会员数
        Long todayActiveUser = memberService.getActiveUserCount(merchantId, storeId, beginTime, endTime);

        // 总支付人数
        Integer totalPayUser = orderService.getPayUserCount(merchantId, storeId);

        Map<String, Object> result = new HashMap<>();

        result.put("todayUser", todayUser);
        result.put("totalUser", totalUser);
        result.put("todayOrder", todayOrder);
        result.put("totalOrder", totalOrder);
        result.put("todayPay", todayPay);
        result.put("totalPay", totalPay);
        result.put("todayActiveUser", todayActiveUser);
        result.put("totalPayUser", totalPayUser);

        return getSuccessResult(result);
    }

    /**
     * 首页图表统计数据
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "首页图表统计数据")
    @RequestMapping(value = "/statistic", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject statistic(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String tag = request.getParameter("tag") == null ? "order,user_active" : request.getParameter("tag");
        Integer storeId = StringUtil.isEmpty(request.getParameter("storeId")) ? 0 : Integer.parseInt(request.getParameter("storeId"));

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        TAccount account = accountService.getAccountInfoById(accountInfo.getId());
        Integer merchantId = account.getMerchantId() == null ? 0 : account.getMerchantId();
        if (account.getStoreId() != null && account.getStoreId() > 0) {
            storeId = account.getStoreId();
        }

        ArrayList<String> days = TimeUtils.getDays(5);
        days.add("昨天");
        days.add("今天");

        Map<String, Object> result = new HashMap<>();
        if (tag.equals("payment")) {
            BigDecimal[] orderPayData = {new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0")};
            for (int i = 0; i < 7; i++) {
                Date beginTime = DateUtil.getDayBegin((6 - i));
                Date endTime = DateUtil.getDayEnd((6 - i));
                BigDecimal payMoney = orderService.getPayMoney(merchantId, storeId, beginTime, endTime);
                orderPayData[i] = payMoney == null ? new BigDecimal("0") : payMoney;
            }
            BigDecimal data[][] = { orderPayData };
            result.put("data", data);
        } else {
            BigDecimal[] orderCountData = {new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0")};
            BigDecimal[] userCountData = {new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0")};

            for (int i = 0; i < 7; i++) {
                Date beginTime = DateUtil.getDayBegin((6 - i));
                Date endTime = DateUtil.getDayEnd((6 - i));
                orderCountData[i] = orderService.getOrderCount(merchantId, storeId, beginTime, endTime);
                Long userCount = memberService.getActiveUserCount(merchantId, storeId, beginTime, endTime);
                userCountData[i] = new BigDecimal(userCount);
            }
            BigDecimal data[][] = { orderCountData, userCountData };
            result.put("data", data);
        }

        result.put("labels", days);

        return getSuccessResult(result);
    }

    /**
     * 获取收款结果
     *
     * @param request
     * @return
     */
    @ApiOperation(value = "获取收款结果")
    @RequestMapping(value = "/cashierResult", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject cashierResult(HttpServletRequest request) throws BusinessCheckException {
        Integer orderId = request.getParameter("orderId") == null ? 0 : Integer.parseInt(request.getParameter("orderId"));

        UserOrderDto orderInfo = orderService.getOrderById(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("orderInfo", orderInfo);

        return getSuccessResult(result);
    }
}
