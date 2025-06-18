package com.fuint.module.clientApi.controller;

import com.alipay.api.AlipayApiException;
import com.fuint.common.dto.*;
import com.fuint.common.enums.OrderStatusEnum;
import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.wxpay.WxPayApiConfigKit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="会员端-支付相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/clientApi/pay")
public class ClientPayController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ClientPayController.class);

    /**
     * 微信服务接口
     * */
    private WeixinService weixinService;

    /**
     * 支付宝服务接口
     * */
    private AlipayService alipayService;

    /**
     * 支付服务接口
     * */
    private PaymentService paymentService;

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 系统设置接口
     * */
    private SettingService settingService;

    /**
     * 会员卡券接口
     * */
    private UserCouponService userCouponService;

    /**
     * 会员等级接口
     * */
    private UserGradeService userGradeService;

    /**
     * 商户服务接口
     */
    private MerchantService merchantService;

    /**
     * 支付前查询
     * */
    @ApiOperation(value = "支付前查询")
    @RequestMapping(value = "/prePay", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject prePay(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer storeId = StringUtil.isEmpty(request.getHeader("storeId")) ? 0 : Integer.parseInt(request.getHeader("storeId"));
        String useFor = request.getParameter("type") == null ? "" : request.getParameter("type");
        String merchantNo = request.getHeader("merchantNo");

        UserInfo userInfo = TokenUtil.getUserInfoByToken(token);
        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        Map<String, Object> outParams = new HashMap<>();

        List<MtSetting> settingList = settingService.getSettingList(mtUser.getMerchantId(), SettingTypeEnum.POINT.getKey());
        String canUsedAsMoney = YesOrNoEnum.FALSE.getKey();
        String exchangeNeedPoint = "0";
        for (MtSetting setting : settingList) {
            if (setting.getName().equals("canUsedAsMoney")) {
                canUsedAsMoney = setting.getValue();
            } else if (setting.getName().equals("exchangeNeedPoint")) {
                exchangeNeedPoint = setting.getValue();
            }
        }

        // 可用卡券
        CouponDto canUseCouponInfo = null;
        if (mtUser != null) {
            List<CouponDto> couponList = userCouponService.getPayAbleCouponList(mtUser.getId(), storeId, useFor);
            if (couponList.size() > 0) {
                canUseCouponInfo = couponList.get(0);
            }
        }

        // 会员折扣
        BigDecimal payDiscount = new BigDecimal("1");
        Integer merchantId = merchantService.getMerchantId(merchantNo);
        if (mtUser != null && !mtUser.getIsStaff().equals(YesOrNoEnum.YES.getKey())) {
            MtUserGrade userGrade = userGradeService.queryUserGradeById(merchantId, Integer.parseInt(mtUser.getGradeId()), mtUser.getId());
            if (userGrade != null) {
                if (userGrade.getDiscount() > 0) {
                    payDiscount = new BigDecimal(userGrade.getDiscount()).divide(new BigDecimal("10"), BigDecimal.ROUND_CEILING, 4);
                    if (payDiscount.compareTo(new BigDecimal("0")) <= 0) {
                        payDiscount = new BigDecimal("1");
                    }
                }
            }
        }

        // 可用积分
        Integer canUsePointAmount = 0;
        if (mtUser != null && canUsedAsMoney.equals(YesOrNoEnum.TRUE.getKey())) {
            canUsePointAmount = mtUser.getPoint();
        }

        outParams.put("canUsedAsMoney", canUsedAsMoney);
        outParams.put("exchangeNeedPoint", exchangeNeedPoint);
        outParams.put("canUsePointAmount", canUsePointAmount);
        outParams.put("canUseCouponInfo", canUseCouponInfo);
        outParams.put("canUseCouponInfo", canUseCouponInfo);
        outParams.put("payDiscount", payDiscount);

        return getSuccessResult(outParams);
    }

    /**
     * 发起支付
     * */
    @ApiOperation(value = "发起支付")
    @RequestMapping(value = "/doPay", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject doPay(HttpServletRequest request) throws BusinessCheckException {
       Map<String, Object> result = paymentService.doPay(request);
       return getSuccessResult(result);
    }

    /**
     * 微信支付回调
     */
    @RequestMapping(value = "/weixinCallback", method = RequestMethod.POST)
    @CrossOrigin
    public void weixinCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("微信支付结果回调....");

        Map<String, String> resData = weixinService.processResXml(request);
        logger.info("微信返回Map:" + resData);
        if (!CollectionUtils.isEmpty(resData)) {
            String orderSn = resData.get("out_trade_no"); // 商户订单号
            String orderId = resData.get("transaction_id"); // 微信交易单号
            String tranAmt = resData.get("total_fee"); // 交易金额
            BigDecimal tranAmount = new BigDecimal(tranAmt).divide(new BigDecimal("100"), BigDecimal.ROUND_CEILING, 4);
            // 参数校验
            if (StringUtil.isNotEmpty(orderSn) && StringUtil.isNotEmpty(tranAmt) && StringUtil.isNotEmpty(orderId)) {
                UserOrderDto orderInfo = orderService.getOrderByOrderSn(orderSn);
                if (orderInfo != null) {
                    String result = resData.get("return_code");
                    if (!result.equals("SUCCESS")) {
                        logger.error("微信支付回调接口验签失败");
                        return;
                    }
                    // 订单金额
                    BigDecimal payAmount = orderInfo.getPayAmount();
                    int compareFlag = tranAmount.compareTo(payAmount);
                    if (compareFlag == 0) { // 支付金额正确
                        if (orderInfo.getStatus().equals(OrderStatusEnum.CREATED.getKey())) {
                            boolean flag = paymentService.paymentCallback(orderInfo);
                            logger.info("回调结果：" + flag);
                            if (flag) {
                                weixinService.processRespXml(response, true);
                            } else {
                                weixinService.processRespXml(response, false);
                            }
                        } else {
                            logger.error("订单{}已经支付，orderInfo.getStatus() = {}, CREATED.getKey() = {}", orderSn, orderInfo.getStatus(), OrderStatusEnum.CREATED.getKey());
                        }
                    } else {
                        logger.error("回调金额与支付金额不匹配 tranAmount = {}, payAmount = {}, compareFlag = {}", tranAmount, orderInfo.getPayAmount(), compareFlag);
                    }
                } else {
                    logger.error("支付订单{}对应的信息不存在", orderSn);
                }
            }
        }
    }

    /**
     * 微信退款通知
     */
    @RequestMapping(value = "/weixinRefundNotify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String weixinRefundNotify(HttpServletRequest request) {
        String xmlMsg = HttpKit.readData(request);
        logger.info("退款通知 = " + xmlMsg);
        Map<String, String> params = WxPayKit.xmlToMap(xmlMsg);
        String returnCode = params.get("return_code");
        // 注意重复通知的情况，同一订单号可能收到多次通知，请注意一定先判断订单状态
        if (WxPayKit.codeIsOk(returnCode)) {
            String reqInfo = params.get("req_info");
            String decryptData = WxPayKit.decryptData(reqInfo, WxPayApiConfigKit.getWxPayApiConfig().getPartnerKey());
            logger.info("退款通知解密后的数据 = " + decryptData);
            // 发送通知等
            Map<String, String> xml = new HashMap<>(2);
            xml.put("return_code", "SUCCESS");
            xml.put("return_msg", "OK");
            return WxPayKit.toXml(xml);
        }
        return null;
    }

    /**
     * 支付宝支付回调
     */
    @RequestMapping(value = "/aliPayCallback", method = RequestMethod.POST)
    @CrossOrigin
    public String aliPayCallback(HttpServletRequest request) throws Exception {
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = AliPayApi.toMap(request);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                logger.info("{} = {}", entry.getKey(), entry.getValue());
            }
            String orderSn = params.get("out_trade_no") != null ? params.get("out_trade_no") : "";
            if (StringUtil.isEmpty(orderSn)) {
                logger.error("支付宝验证失败 订单号为空");
            }
            Boolean verifyResult = alipayService.checkCallBack(params);
            if (verifyResult) {
                logger.info("支付宝验证成功 success");
                UserOrderDto orderInfo = orderService.getOrderByOrderSn(orderSn);
                Boolean flag = paymentService.paymentCallback(orderInfo);
                if (flag) {
                    return "success";
                } else {
                    return "failure";
                }
            } else {
                logger.error("支付宝验证失败 orderSn={}", orderSn);
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            logger.error("支付宝回调出错啦...");
            return "failure";
        }
    }
}
