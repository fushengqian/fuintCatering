package com.fuint.module.riderApi.controller;

import com.fuint.common.dto.member.UserInfo;
import com.fuint.common.dto.rider.RiderDto;
import com.fuint.common.dto.rider.RiderOrderDto;
import com.fuint.common.param.RiderMyOrderParam;
import com.fuint.common.param.RiderOrderSearchParam;
import com.fuint.common.param.RiderOrderPoolParam;
import com.fuint.common.service.RiderOrderService;
import com.fuint.common.service.RiderService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 骑手端-订单相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags = "骑手端-订单相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/riderApi/order")
public class RiderOrderController extends BaseController {

    private RiderOrderService riderOrderService;

    private RiderService riderService;

    /**
     * 获取待接单订单池
     */
    @ApiOperation(value = "获取待接单订单池")
    @RequestMapping(value = "/pool", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject pool(@RequestBody RiderOrderPoolParam param) throws BusinessCheckException {
        RiderDto rider = getRider();
        PaginationResponse response = riderOrderService.getOrderPool(param, rider.getStoreId());
        return getSuccessResult(response);
    }

    /**
     * 骑手接单
     */
    @ApiOperation(value = "骑手接单")
    @RequestMapping(value = "/accept", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject accept(@RequestParam("orderId") Integer orderId) throws BusinessCheckException {
        RiderDto rider = getRider();
        RiderOrderDto result = riderOrderService.acceptOrder(orderId, rider.getId(), rider.getMerchantId());
        return getSuccessResult(result);
    }

    /**
     * 获取骑手配送订单列表
     */
    @ApiOperation(value = "获取骑手配送订单列表")
    @RequestMapping(value = "/myOrders", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject myOrders(@RequestBody RiderMyOrderParam param) throws BusinessCheckException {
        RiderDto rider = getRider();
        PaginationResponse response = riderOrderService.getMyOrders(param, rider.getId());
        return getSuccessResult(response);
    }

    /**
     * 获取订单详情
     */
    @ApiOperation(value = "获取订单详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject detail(HttpServletRequest request) throws BusinessCheckException {
        String orderId = request.getParameter("orderId");
        if (StringUtil.isEmpty(orderId)) {
            return getFailureResult(201, "订单不能为空");
        }
        RiderOrderDto orderDetail = riderOrderService.getOrderDetail(Integer.parseInt(orderId));
        return getSuccessResult(orderDetail);
    }

    /**
     * 确认取货
     */
    @ApiOperation(value = "确认取货")
    @RequestMapping(value = "/pickup", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject pickup(@RequestParam("orderId") Integer orderId) throws BusinessCheckException {
        RiderDto rider = getRider();
        RiderOrderDto result = riderOrderService.confirmPickup(orderId, rider.getId());
        return getSuccessResult(result);
    }

    /**
     * 确认送达
     */
    @ApiOperation(value = "确认送达")
    @RequestMapping(value = "/deliver", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject deliver(@RequestParam("orderId") Integer orderId) throws BusinessCheckException {
        RiderDto rider = getRider();
        RiderOrderDto result = riderOrderService.confirmDeliver(orderId, rider.getId());
        return getSuccessResult(result);
    }

    /**
     * 搜索已完成配送订单
     */
    @ApiOperation(value = "搜索已完成配送订单")
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject search(@RequestBody RiderOrderSearchParam param) throws BusinessCheckException {
        RiderDto rider = getRider();
        PaginationResponse response = riderOrderService.searchCompletedOrders(param, rider.getId());
        return getSuccessResult(response);
    }

    /**
     * 获取骑手信息并校验身份
     */
    private RiderDto getRider() throws BusinessCheckException {
        UserInfo userInfo = TokenUtil.getUserInfo();
        if (userInfo == null) {
            throw new BusinessCheckException("用户未登录");
        }
        RiderDto rider = riderService.getRiderByUserId(userInfo.getId());
        if (rider == null) {
            throw new BusinessCheckException("非骑手身份，无法操作");
        }
        if (!"A".equals(rider.getStatus())) {
            throw new BusinessCheckException("骑手状态异常");
        }
        return rider;
    }
}
