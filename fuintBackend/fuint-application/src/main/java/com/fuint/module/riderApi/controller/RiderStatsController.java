package com.fuint.module.riderApi.controller;

import com.fuint.common.dto.member.UserInfo;
import com.fuint.common.dto.rider.RiderDto;
import com.fuint.common.dto.rider.RiderStatsDto;
import com.fuint.common.service.RiderOrderService;
import com.fuint.common.service.RiderService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 骑手端-统计相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags = "骑手端-统计相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/riderApi/stats")
public class RiderStatsController extends BaseController {

    private RiderOrderService riderOrderService;

    private RiderService riderService;

    /**
     * 获取今日概览
     */
    @ApiOperation(value = "获取今日概览")
    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject overview() throws BusinessCheckException {
        RiderDto rider = getRider();
        Map<String, Object> result = riderOrderService.getTodayOverview(rider.getId());
        return getSuccessResult(result);
    }

    /**
     * 获取配送统计
     */
    @ApiOperation(value = "获取配送统计")
    @RequestMapping(value = "/delivery", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject delivery(HttpServletRequest request) throws BusinessCheckException {
        RiderDto rider = getRider();
        String mode = request.getParameter("mode");
        if (mode == null || mode.isEmpty()) {
            mode = "week";
        }
        RiderStatsDto stats = riderOrderService.getRiderStats(rider.getId(), mode);
        return getSuccessResult(stats);
    }

    /**
     * 获取收入统计
     */
    @ApiOperation(value = "获取收入统计")
    @RequestMapping(value = "/income", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject income(HttpServletRequest request) throws BusinessCheckException {
        RiderDto rider = getRider();
        String mode = request.getParameter("mode");
        if (mode == null || mode.isEmpty()) {
            mode = "week";
        }
        RiderStatsDto stats = riderOrderService.getRiderStats(rider.getId(), mode);
        return getSuccessResult(stats);
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
        return rider;
    }
}
