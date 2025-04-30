package com.fuint.module.clientApi.controller;

import com.fuint.common.param.MemberInfoParam;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.MerchantService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtUser;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 收银台controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="会员端-收银台相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/clientApi/cashier")
public class ClientCashierController extends BaseController {

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 商户服务接口
     */
    private MerchantService merchantService;

    /**
     * 获取会员信息
     */
    @ApiOperation(value = "查询会员信息")
    @RequestMapping(value = "/memberInfo", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject memberInfo(HttpServletRequest request,  @RequestBody MemberInfoParam memberInfoParam) throws BusinessCheckException {
        String merchantNo = request.getHeader("merchantNo") == null ? "" : request.getHeader("merchantNo");
        Integer merchantId = merchantService.getMerchantId(merchantNo);

        String mobile = memberInfoParam.getMobile() == null ? "" : memberInfoParam.getMobile();
        if (StringUtil.isEmpty(mobile)) {
            return getFailureResult(201);
        }

        MtUser userInfo = memberService.queryMemberByMobile(merchantId, mobile);
        Map<String, Object> outParams = new HashMap<>();
        outParams.put("memberInfo", userInfo);

        return getSuccessResult(outParams);
    }

}
