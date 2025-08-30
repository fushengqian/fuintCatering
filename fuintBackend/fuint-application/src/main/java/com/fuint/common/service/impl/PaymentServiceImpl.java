package com.fuint.common.service.impl;

import com.fuint.common.dto.*;
import com.fuint.common.enums.*;
import com.fuint.common.service.*;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.mapper.MtOrderMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private MtOrderMapper mtOrderMapper;

    /**
     * 微信服务接口
     * */
    private WeixinService weixinService;

    /**
     * 支付宝服务接口
     * */
    private AlipayService alipayService;

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 余额服务接口
     * */
    private BalanceService balanceService;

    /**
     * 会员卡券服务接口
     * */
    private UserCouponService userCouponService;

    /**
     * 创建预支付订单
     *
     * @param userInfo 会员信息
     * @param orderInfo 订单信息
     * @param payAmount 支付金额
     * @param authCode 付款码
     * @param giveAmount 赠送金额
     * @param ip 支付IP地址
     * @param platform 支付平台
     * @param isWechat 是否微信客户端
     * @return
     * */
    @Override
    public ResponseObject createPrepayOrder(MtUser userInfo, MtOrder orderInfo, Integer payAmount, String authCode, Integer giveAmount, String ip, String platform, String isWechat) throws BusinessCheckException {
        logger.info("PaymentService createPrepayOrder inParams userInfo={} payAmount={} giveAmount={} goodsInfo={}", userInfo, payAmount, giveAmount, orderInfo);

        ResponseObject responseObject;
        if (orderInfo.getPayType().equals(PayTypeEnum.ALISCAN.getKey())) {
            // 支付宝支付
            responseObject = alipayService.createPrepayOrder(userInfo, orderInfo, payAmount, authCode, giveAmount, ip, platform);
        } else {
            // 微信支付
            responseObject = weixinService.createPrepayOrder(userInfo, orderInfo, payAmount, authCode, giveAmount, ip, platform, isWechat);
        }

        logger.info("PaymentService createPrepayOrder outParams {}", responseObject.toString());
        return responseObject;
    }

    /**
     * 支付成功回调
     *
     * @param orderInfo 订单信息
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean paymentCallback(UserOrderDto orderInfo) throws BusinessCheckException {
        logger.info("paymentCallback outParams {}", orderInfo.toString());
        MtOrder mtOrder = mtOrderMapper.selectById(orderInfo.getId());

        // 更新订单状态为已支付
        Boolean isPay = orderService.setOrderPayed(orderInfo.getId(), null);
        if (mtOrder == null || !isPay) {
            return false;
        }

        // 储值卡订单
        if (orderInfo.getType().equals(OrderTypeEnum.PRESTORE.getKey())) {
            Map<String, Object> param = new HashMap<>();
            param.put("couponId", orderInfo.getCouponId());
            param.put("userId", orderInfo.getUserId());
            param.put("param", orderInfo.getParam());
            param.put("orderId", orderInfo.getId());
            userCouponService.preStore(param);
        }

        // 充值订单
        if (orderInfo.getType().equals(OrderTypeEnum.RECHARGE.getKey())) {
            // 余额支付
            MtBalance mtBalance = new MtBalance();
            OrderUserDto userDto = orderInfo.getUserInfo();
            if (userDto.getMobile() != null && StringUtil.isNotEmpty(userDto.getMobile())) {
                mtBalance.setMobile(userDto.getMobile());
            }
            mtBalance.setOrderSn(orderInfo.getOrderSn());
            mtBalance.setUserId(orderInfo.getUserId());
            mtBalance.setMerchantId(orderInfo.getMerchantId());
            String param = orderInfo.getParam();
            if (StringUtil.isNotEmpty(param)) {
                String params[] = param.split("_");
                if (params.length == 2) {
                    BigDecimal amount = new BigDecimal(params[0]).add(new BigDecimal(params[1]));
                    mtBalance.setAmount(amount);
                    balanceService.addBalance(mtBalance, true);
                }
            }
        }

        logger.info("PaymentService paymentCallback Success orderSn {}", orderInfo.getOrderSn());
        return true;
    }

    /**
     * 发起支付
     *
     * @param request 请求参数
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> doPay(HttpServletRequest request) throws BusinessCheckException {
        String platform = request.getHeader("platform") == null ? "" : request.getHeader("platform");
        String isWechat = request.getHeader("isWechat") == null ? "" : request.getHeader("isWechat");
        String payType = request.getParameter("payType") == null ? PayTypeEnum.JSAPI.getKey() : request.getParameter("payType");
        String cashierPayAmount = request.getParameter("cashierPayAmount") == null ? "" : request.getParameter("cashierPayAmount"); // 收银台实付金额
        String cashierDiscountAmount = request.getParameter("cashierDiscountAmount") == null ? "" : request.getParameter("cashierDiscountAmount"); // 收银台优惠金额
        UserInfo loginInfo = TokenUtil.getUserInfoByToken(request.getHeader("Access-Token"));
        String orderId = request.getParameter("orderId");
        String userId = request.getParameter("userId");
        String authCode = request.getParameter("authCode");
        if (StringUtil.isEmpty(orderId)) {
            throw new BusinessCheckException("订单不能为空");
        }
        if (orderId.length() >= 12) {
            MtOrder mtOrder = orderService.getOrderInfoByOrderSn(orderId);
            if (mtOrder != null) {
                orderId = mtOrder.getId().toString();
            }
        }
        MtOrder orderInfo = mtOrderMapper.selectById(Integer.parseInt(orderId));
        if (orderInfo == null) {
            throw new BusinessCheckException("该订单不存在");
        }
        MtUser mtUser = null;
        if (loginInfo != null) {
            mtUser = memberService.queryMemberById(loginInfo.getId());
        }

        // 重新生成订单号
        String orderSn = CommonUtil.createOrderSN(orderInfo.getUserId().toString());
        orderInfo.setOrderSn(orderSn);
        orderInfo.setPayType(payType);
        orderInfo.setPlatform(platform);
        orderInfo = orderService.updateOrder(orderInfo);

        // 收银员操作
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));
        if (loginInfo == null && accountInfo != null) {
            // 游客订单绑定到会员
            if (orderInfo.getIsVisitor().equals(YesOrNoEnum.YES.getKey()) && StringUtil.isNotEmpty(userId)) {
                mtUser = memberService.queryMemberById(Integer.parseInt(userId));
                orderInfo.setUserId(Integer.parseInt(userId));
                orderInfo.setIsVisitor(YesOrNoEnum.NO.getKey());
                orderService.updateOrder(orderInfo);
            } else {
                mtUser = memberService.queryMemberById(orderInfo.getUserId());
            }
        }

        if (mtUser == null) {
            throw new BusinessCheckException("登录信息失效");
        }

        if (accountInfo != null && StringUtil.isNotEmpty(cashierPayAmount) && StringUtil.isNotEmpty(cashierDiscountAmount)) {
            orderInfo.setDiscount(new BigDecimal(cashierDiscountAmount));
            if (loginInfo == null) {
                MtUser user = memberService.queryMemberById(orderInfo.getUserId());
                if (user != null) {
                    loginInfo = new UserInfo();
                    loginInfo.setId(user.getId());
                }
            }
        }

        // 实付金额 = 总金额 - 优惠金额 - 积分金额
        BigDecimal realPayAmount = orderInfo.getAmount().subtract(new BigDecimal(orderInfo.getDiscount().toString())).subtract(new BigDecimal(orderInfo.getPointAmount().toString())).add(orderInfo.getDeliveryFee());
        Object payment = null;
        Boolean isPaying = false;
        if (payType.equals(PayTypeEnum.BALANCE.getKey())) {
            if (orderInfo.getType().equals(OrderTypeEnum.PRESTORE.getKey()) || orderInfo.getType().equals(OrderTypeEnum.RECHARGE.getKey())) {
                throw new BusinessCheckException("抱歉，不能使用余额支付");
            }
            // 余额支付
            MtBalance balance = new MtBalance();
            balance.setMobile(mtUser.getMobile());
            balance.setOrderSn(orderInfo.getOrderSn());
            balance.setStoreId(orderInfo.getStoreId());
            balance.setUserId(mtUser.getId());
            BigDecimal balanceAmount = realPayAmount.subtract(realPayAmount).subtract(realPayAmount);
            balance.setAmount(balanceAmount);
            boolean isPay = balanceService.addBalance(balance, true);
            if (isPay) {
                orderService.setOrderPayed(orderInfo.getId(), balanceAmount);
                OrderDto reqOrder = new OrderDto();
                reqOrder.setId(orderInfo.getId());
                reqOrder.setPayAmount(realPayAmount);
                reqOrder.setDiscount(orderInfo.getDiscount());
                reqOrder.setPayType(PayTypeEnum.BALANCE.getKey());
                if (accountInfo != null) {
                    reqOrder.setOperator(accountInfo.getAccountName());
                }
                orderService.updateOrder(reqOrder);
                orderInfo = orderService.getOrderInfo(orderInfo.getId());
            } else {
                throw new BusinessCheckException("会员余额不足");
            }
        } else if (payType.equals(PayTypeEnum.CASH.getKey()) && accountInfo != null) {
            // 现金支付
            OrderDto reqOrder = new OrderDto();
            reqOrder.setId(orderInfo.getId());
            reqOrder.setAmount(new BigDecimal(cashierPayAmount).add(new BigDecimal(cashierDiscountAmount)));
            reqOrder.setDiscount(new BigDecimal(cashierDiscountAmount));
            reqOrder.setPayAmount(new BigDecimal(cashierPayAmount));
            reqOrder.setPayTime(new Date());
            reqOrder.setPayType(PayTypeEnum.CASH.getKey());
            reqOrder.setOperator(accountInfo.getAccountName());
            orderService.updateOrder(reqOrder);
            orderService.setOrderPayed(orderInfo.getId(), null);
            orderInfo = orderService.getOrderInfo(orderInfo.getId());
        } else {
            String ip = CommonUtil.getIPFromHttpRequest(request);
            BigDecimal pay = realPayAmount.multiply(new BigDecimal("100"));
            orderInfo.setPayType(payType);
            ResponseObject paymentInfo = createPrepayOrder(mtUser, orderInfo, (pay.intValue()), authCode, 0, ip, platform, isWechat);
            if (paymentInfo.getData() == null) {
                if (paymentInfo.getCode() == 3000) {
                    logger.info("需要用户输入密码..");
                    isPaying = true;
                } else {
                    throw new BusinessCheckException("抱歉，支付失败");
                }
            }
            payment = paymentInfo.getData();
        }

        Map<String, Object> result = new HashMap();
        result.put("isCreated", true);
        result.put("payType", payType);
        result.put("orderInfo", orderInfo);
        result.put("payment", payment);
        result.put("isPaying", isPaying);

        return result;
    }
}
