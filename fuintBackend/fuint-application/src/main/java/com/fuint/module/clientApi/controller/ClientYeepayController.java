package com.fuint.module.clientApi.controller;

import com.alibaba.fastjson.JSONObject;
import com.fuint.common.dto.UserInfo;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.YeepayService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtOrder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

/**
 * 易宝支付客户端接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags = "易宝支付客户端接口")
@RestController
@RequestMapping(value = "/clientApi/yeepay")
public class ClientYeepayController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ClientYeepayController.class);

    @Autowired
    private YeepayService yeepayService;

    @Autowired
    private OrderService orderService;

    /**
     * 获取用户信息
     *
     * @param request HTTP请求
     * @return 用户信息
     */
    private UserInfo getUserInfo(HttpServletRequest request) {
        Object userObj = request.getAttribute("userInfo");
        if (userObj == null) {
            return null;
        }
        return (UserInfo) userObj;
    }

    /**
     * 发起易宝支付
     *
     * @param request     HTTP请求
     * @param requestBody 请求参数
     * @return 支付结果，包含预支付标识（二维码数据）
     */
    @ApiOperation("发起易宝支付")
    @RequestMapping(value = "/initiate-payment", method = RequestMethod.POST)
    @CrossOrigin
    @ResponseBody
    public ResponseObject initiatePayment(HttpServletRequest request, @RequestBody JSONObject requestBody) {
        try {
            // 获取用户信息
            UserInfo userInfo = getUserInfo(request);
            if (userInfo == null) {
                return getFailureResult(201, "用户未登录");
            }

            // 参数校验
            if (requestBody == null) {
                return getFailureResult(201, "请求参数不能为空");
            }

            String orderId = requestBody.getString("orderId");
            String payWay = requestBody.getString("payWay");
            String channel = requestBody.getString("channel");

            if (orderId == null || orderId.isEmpty()) {
                return getFailureResult(201, "订单ID不能为空");
            }
            if (payWay == null || payWay.isEmpty()) {
                return getFailureResult(201, "支付方式不能为空");
            }
            if (channel == null || channel.isEmpty()) {
                return getFailureResult(201, "支付渠道不能为空");
            }

            // 记录请求日志
            logger.info("发起易宝支付请求，用户ID：{}，订单ID：{}，支付方式：{}，渠道：{}",
                    userInfo.getId(), orderId, payWay, channel);

            // 查询订单信息
            UserOrderDto orderDto = orderService.getOrderById(Integer.parseInt(orderId));
            if (orderDto == null) {
                return getFailureResult(201, "订单不存在");
            }

            // 检查订单所属用户
            if (!orderDto.getUserId().equals(userInfo.getId())) {
                return getFailureResult(201, "无权操作此订单");
            }

            // 检查订单状态
            String orderStatus = orderDto.getStatus();
            if (!"CREATED".equals(orderStatus) && !"PENDING".equals(orderStatus)) {
                return getFailureResult(201, "订单状态不允许支付");
            }

            // 创建MtOrder对象
            MtOrder order = new MtOrder();
            order.setId(orderDto.getId());
            order.setOrderSn(orderDto.getOrderSn());
            order.setAmount(orderDto.getAmount());
            order.setStatus(orderDto.getStatus());
            order.setUserId(orderDto.getUserId());
            order.setMerchantId(orderDto.getMerchantId());
            order.setStoreId(orderDto.getStoreId());

            // 设置支付相关信息
            order.setPayType(payWay);
            order.setPaymentGateway("YEEPAY");

            // 调用易宝支付服务
            YeepayPaymentResponse paymentResponse = yeepayService.initiatePayment(order);

            // 记录响应日志
            logger.info("易宝支付响应，订单ID：{}，响应码：{}，消息：{}",
                    orderId, paymentResponse.getCode(), paymentResponse.getMessage());

            // 返回支付结果
            if ("SUCCESS".equals(paymentResponse.getCode())) {
                return getSuccessResult("支付请求成功", paymentResponse);
            } else {
                return getFailureResult(201, paymentResponse.getMessage(), paymentResponse);
            }
        } catch (BusinessCheckException e) {
            logger.error("发起易宝支付业务异常，异常：{}", e.getMessage());
            return getFailureResult(201, e.getMessage());
        } catch (Exception e) {
            logger.error("发起易宝支付异常，异常：{}", e.getMessage(), e);
            return getFailureResult(500, "支付请求处理异常");
        }
    }

    /**
     * 接收易宝支付通知
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return 处理结果
     */
    @RequestMapping(value = "/payment-notify", method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(HttpServletRequest request, HttpServletResponse response) {
        logger.info("接收到易宝支付通知，时间：{}", new Date());

        try {
            // 获取请求体内容
            String notificationData = getRequestBody(request);

            // 记录通知信息
            logger.info("接收到易宝支付通知：{}",
                    notificationData.length() > 500 ? notificationData.substring(0, 500) + "..." : notificationData);

            // 调用YeepayService处理通知
            YeepayNotificationResult result = yeepayService.processNotification(notificationData);

            // 记录处理结果
            if (result != null) {
                logger.info("易宝支付通知处理成功，订单号：{}",
                        result.getOrderId() != null ? result.getOrderId() : "未知");
                return "success";
            } else {
                logger.warn("易宝支付通知处理失败");
                return "fail";
            }
        } catch (Exception e) {
            logger.error("处理易宝支付通知异常", e);
            return "fail";
        }
    }

    /**
     * 查询易宝支付订单状态
     *
     * @param request HTTP请求
     * @param orderId 订单ID
     * @return 订单状态
     */
    @ApiOperation("查询易宝支付订单状态")
    @RequestMapping(value = "/query-order", method = RequestMethod.GET)
    @CrossOrigin
    @ResponseBody
    public ResponseObject queryOrder(HttpServletRequest request, @RequestParam String orderId) {
        try {
            // 获取用户信息
            UserInfo userInfo = getUserInfo(request);
            if (userInfo == null) {
                return getFailureResult(201, "用户未登录");
            }

            // 参数校验
            if (orderId == null || orderId.isEmpty()) {
                return getFailureResult(201, "订单ID不能为空");
            }

            // 查询订单信息
            UserOrderDto orderDto = orderService.getOrderById(Integer.parseInt(orderId));
            if (orderDto == null) {
                return getFailureResult(201, "订单不存在");
            }

            // 检查订单所属用户
            if (!orderDto.getUserId().equals(userInfo.getId())) {
                return getFailureResult(201, "无权查询此订单");
            }

            // 构建返回数据
            JSONObject data = new JSONObject();
            data.put("orderId", orderDto.getId());
            data.put("orderSn", orderDto.getOrderSn());
            data.put("status", orderDto.getStatus());
            data.put("yeepayStatus", orderDto.getParam() != null && orderDto.getParam().contains("yeepayStatus") ?
                    JSONObject.parseObject(orderDto.getParam()).getString("yeepayStatus") : null);
            data.put("yeepayTransactionId", orderDto.getParam() != null && orderDto.getParam().contains("yeepayTransactionId") ?
                    JSONObject.parseObject(orderDto.getParam()).getString("yeepayTransactionId") : null);

            return getSuccessResult("查询成功", data);
        } catch (Exception e) {
            logger.error("查询易宝支付订单状态异常，异常：{}", e.getMessage(), e);
            return getFailureResult(500, "查询订单状态异常");
        }
    }

    /**
     * 获取请求体内容
     *
     * @param request HTTP请求
     * @return 请求体内容
     * @throws IOException IO异常
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        return requestBody.toString();
    }
}
