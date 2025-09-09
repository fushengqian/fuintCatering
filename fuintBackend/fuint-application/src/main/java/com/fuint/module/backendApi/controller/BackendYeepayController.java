package com.fuint.module.backendApi.controller;

import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.dto.YeepayNotificationResult;
import com.fuint.common.dto.YeepayPaymentResponse;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.YeepayService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.module.backendApi.request.YeepayPaymentRequest;
import com.fuint.repository.mapper.MtYeepayNotificationLogMapper;
import com.fuint.repository.model.MtOrder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

/**
 * 易宝支付相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags = "易宝支付相关接口")
@RestController
@RequestMapping(value = "/backendApi/yeepay")
public class BackendYeepayController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BackendYeepayController.class);

    @Autowired
    private YeepayService yeepayService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private MtYeepayNotificationLogMapper yeepayNotificationLogMapper;

    /**
     * 发起易宝支付
     *
     * @param request 支付请求参数
     * @return 支付结果，包含预支付标识（二维码数据）
     */
    @ApiOperation("发起易宝支付")
    @RequestMapping(value = "/initiate-payment", method = RequestMethod.POST)
    @ResponseBody
    public ResponseObject initiatePayment(@RequestBody YeepayPaymentRequest request) {
        try {
            // 参数校验
            if (request == null) {
                return getFailureResult(201, "请求参数不能为空");
            }
            
            Integer orderId = request.getOrderId();
            String payWay = request.getPayWay();
            String channel = request.getChannel();
            
            if (orderId == null || orderId <= 0) {
                return getFailureResult(201, "订单ID不能为空");
            }
            if (payWay == null || payWay.isEmpty()) {
                return getFailureResult(201, "支付方式不能为空");
            }
            if (channel == null || channel.isEmpty()) {
                return getFailureResult(201, "支付渠道不能为空");
            }

            // 记录请求日志
            logger.info("发起易宝支付请求，订单ID：{}，支付方式：{}，渠道：{}", 
                    orderId, payWay, channel);

            // 查询订单信息
            UserOrderDto orderDto = orderService.getOrderById(orderId);
            if (orderDto == null) {
                return getFailureResult(201, "订单不存在");
            }

            // 检查订单状态
            String orderStatus = orderDto.getStatus();
            if (!"CREATED".equals(orderStatus) && !"PENDING".equals(orderStatus)) {
                return getFailureResult(201, "订单状态不允许支付");
            }

            // 将UserOrderDto转换为MtOrder
            MtOrder order = new MtOrder();
            order.setId(orderDto.getId());
            order.setOrderSn(orderDto.getOrderSn());
            order.setMerchantId(orderDto.getMerchantId());
            order.setStoreId(orderDto.getStoreId());
            order.setUserId(orderDto.getUserId());
            order.setAmount(orderDto.getAmount());
            order.setPayAmount(orderDto.getPayAmount());
            order.setStatus(orderStatus);
            
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
     * @param request HTTP请求
     * @return 处理结果
     */
    @RequestMapping(value = "/payment-notify", method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(HttpServletRequest request) {
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
            if (result.isSuccess()) {
                if (result.isProcessed()) {
                    logger.info("易宝支付通知已处理过，订单号：{}，状态：{}", result.getOrderId(), result.getStatus());
                } else {
                    logger.info("易宝支付通知处理成功，订单号：{}，状态：{}", result.getOrderId(), result.getStatus());
                }
            } else {
                logger.warn("易宝支付通知处理失败，消息：{}", result.getMessage());
            }
            
            // 无论处理成功与否，都返回成功响应，避免易宝支付平台重复发送通知
            return "success";
        } catch (Exception e) {
            logger.error("处理易宝支付通知异常", e);
            // 发生异常也返回成功响应，避免易宝支付平台重复发送通知
            return "success";
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