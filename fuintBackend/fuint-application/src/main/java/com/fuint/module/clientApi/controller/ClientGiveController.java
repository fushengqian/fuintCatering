package com.fuint.module.clientApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.GiveDto;
import com.fuint.common.dto.UserInfo;
import com.fuint.common.param.GiveListParam;
import com.fuint.common.param.GiveParam;
import com.fuint.common.service.GiveService;
import com.fuint.common.service.MemberService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
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
 * 卡券转赠controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="会员端-卡券转赠相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/clientApi/give")
public class ClientGiveController extends BaseController {

    /**
     * 转赠服务接口
     */
    private GiveService giveService;

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 转赠卡券
     */
    @ApiOperation(value = "转赠卡券")
    @RequestMapping(value = "/doGive", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject doGive(HttpServletRequest request, @RequestBody GiveParam giveParam) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");

        if (StringUtil.isEmpty(token)) {
            return getFailureResult(1001);
        }

        UserInfo userInfo = TokenUtil.getUserInfoByToken(token);
        if (userInfo == null) {
            return getFailureResult(1001);
        }
        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        giveParam.setUserId(mtUser.getId());
        giveParam.setStoreId(mtUser.getStoreId());
        giveParam.setMerchantId(mtUser.getMerchantId());

        try {
            ResponseObject result = giveService.addGive(giveParam);
            return getSuccessResult(result.getData());
        } catch (BusinessCheckException e) {
            return getFailureResult(3008);
        }
    }

    /**
     * 查询转赠记录
     */
    @ApiOperation(value = "查询转赠记录")
    @RequestMapping(value = "/giveLog", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject giveLog(HttpServletRequest request, @RequestBody GiveListParam giveListParam) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");

        if (StringUtil.isEmpty(token)) {
            return getFailureResult(1001);
        }

        UserInfo mtUser = TokenUtil.getUserInfoByToken(token);

        if (null == mtUser) {
            return getFailureResult(1001);
        }

        String mobile = giveListParam.getMobile() == null ? "" : giveListParam.getMobile();
        String type = giveListParam.getType() == null ? "give" : giveListParam.getType();
        Integer pageNumber = giveListParam.getPage() == null ? Constants.PAGE_NUMBER : giveListParam.getPage();
        Integer pageSize = giveListParam.getPageSize() == null ? Constants.PAGE_SIZE : giveListParam.getPageSize();

        PaginationRequest paginationRequest = new PaginationRequest();
        Map<String, Object> searchParams = new HashMap<>();
        paginationRequest.setCurrentPage(pageNumber);
        paginationRequest.setPageSize(pageSize);

        if (type.equals("gived")) {
            searchParams.put("userId", mtUser.getId());
        } else {
            searchParams.put("giveUserId", mtUser.getId());
        }

        if (StringUtil.isNotEmpty(mobile) && type.equals("give")) {
            searchParams.put("mobile", mobile);
        } else if(StringUtil.isNotEmpty(mobile) && type.equals("gived")) {
            searchParams.put("userMobile", mobile);
        }
        paginationRequest.setSearchParams(searchParams);
        PaginationResponse<GiveDto> paginationResponse = giveService.queryGiveListByPagination(paginationRequest);

        Map<String, Object> outParams = new HashMap();
        outParams.put("content", paginationResponse.getContent());
        outParams.put("pageSize", paginationResponse.getPageSize());
        outParams.put("pageNumber", paginationResponse.getCurrentPage());
        outParams.put("totalRow", paginationResponse.getTotalElements());
        outParams.put("totalPage", paginationResponse.getTotalPages());

        return getSuccessResult(outParams);
    }
}

