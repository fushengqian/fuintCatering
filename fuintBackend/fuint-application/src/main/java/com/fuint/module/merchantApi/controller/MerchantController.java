package com.fuint.module.merchantApi.controller;

import com.fuint.common.dto.member.UserInfo;
import com.fuint.common.dto.merchant.StaffDto;
import com.fuint.common.service.BookItemService;
import com.fuint.common.service.ConfirmLogService;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.RefundService;
import com.fuint.common.service.StaffService;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商户相关controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Tag(name="商户端-商户信息相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/merchantApi/merchant")
public class MerchantController extends BaseController {

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 店铺员工服务接口
     * */
    private StaffService staffService;

    /**
     * 卡券核销记录服务接口
     * */
    private ConfirmLogService confirmLogService;

    /**
     * 售后服务接口
     * */
    private RefundService refundService;

    /**
     * 预约单服务接口
     * */
    private BookItemService bookItemService;

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 查询商户信息
     */
    @Operation(summary="查询商户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject info() throws BusinessCheckException {
        UserInfo userInfo = TokenUtil.getUserInfo();
        if (null == userInfo) {
            return getFailureResult(1001, "用户未登录");
        }

        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        Map<String, Object> outParams = new HashMap<>();
        outParams.put("userInfo", mtUser);

        StaffDto staffInfo = staffService.getStaffInfoByMobile(mtUser.getMobile());
        if (null == staffInfo) {
            return getFailureResult(201, "您的帐号不是商户，没有操作权限");
        }

        outParams.put("confirmInfo", staffInfo);

        // 总收款额
        Date beginTime = DateUtil.getDayBegin();
        Date endTime = DateUtil.getDayEnd();
        BigDecimal payMoney = orderService.getPayMoney(staffInfo.getMerchantId(), staffInfo.getStoreId(), beginTime, endTime);
        outParams.put("payMoney", payMoney);

        // 总会员数
        Long userCount = memberService.getUserCount(staffInfo.getMerchantId(), staffInfo.getStoreId());
        outParams.put("userCount", userCount);

        // 今日订单数
        BigDecimal orderCount = orderService.getOrderCount(staffInfo.getMerchantId(), staffInfo.getStoreId(), beginTime, endTime);
        outParams.put("orderCount", orderCount);

        // 核销券数
        Long confirmCount = confirmLogService.getConfirmCount(staffInfo.getMerchantId(), staffInfo.getStoreId(), beginTime, endTime);
        outParams.put("couponCount", confirmCount);

        // 今日活跃会员数
        Long todayUser = memberService.getActiveUserCount(staffInfo.getMerchantId(), staffInfo.getStoreId(), beginTime, endTime);
        outParams.put("todayUser", todayUser);

        // 今日待核销订单数量（已支付、到店自提、未核销的实物订单，口径与商户端订单列表dataType=todo一致）
        Long todoOrderCount = orderService.getTodoOrderCount(staffInfo.getMerchantId(), staffInfo.getStoreId(), beginTime, endTime);
        outParams.put("todoOrderCount", todoOrderCount);

        // 今日待售后审核数量（待审核的售后单，口径与商户端售后列表status=1一致）
        Long refundTodoCount = refundService.getRefundTodoCount(staffInfo.getMerchantId(), staffInfo.getStoreId(), beginTime, endTime);
        outParams.put("refundTodoCount", refundTodoCount);

        // 今日预约数量（今日提交的预约单，口径与商户端预约列表一致）
        Long bookTodoCount = bookItemService.getBookCount(staffInfo.getMerchantId(), staffInfo.getStoreId(), beginTime, endTime);
        outParams.put("bookTodoCount", bookTodoCount);

        return getSuccessResult(outParams);
    }
}
