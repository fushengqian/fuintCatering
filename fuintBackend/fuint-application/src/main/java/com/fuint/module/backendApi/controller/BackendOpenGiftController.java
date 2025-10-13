package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.OpenGiftDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.OpenGiftService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtOpenGift;
import com.fuint.repository.model.MtUserGrade;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开卡赠礼管理controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-开卡赠礼相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/openGift")
public class BackendOpenGiftController extends BaseController {

    /**
     * 会员服务接口
     */
    private MemberService memberService;

    /**
     * 开卡赠礼服务接口
     */
    private OpenGiftService openGiftService;

    /**
     * 开卡赠礼列表查询
     */
    @ApiOperation(value = "开卡赠礼列表查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('openGift:index')")
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));

        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        String couponId = request.getParameter("couponId");
        String gradeId = request.getParameter("gradeId");
        String status = request.getParameter("status");

        Map<String, Object> param = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            param.put("merchantId", accountInfo.getMerchantId());
        }
        if (StringUtil.isNotEmpty(couponId)) {
            param.put("couponId", couponId);
        }
        if (StringUtil.isNotEmpty(gradeId)) {
            param.put("gradeId", gradeId);
        }
        if (StringUtil.isNotEmpty(status)) {
            param.put("status", status);
        }
        param.put("pageNumber", page);
        param.put("pageSize", pageSize);

        ResponseObject response = openGiftService.getOpenGiftList(param);

        Map<String, Object> params = new HashMap<>();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            params.put("MERCHANT_ID", accountInfo.getMerchantId());
        }
        params.put("STATUS", StatusEnum.ENABLED.getKey());
        List<MtUserGrade> userGradeList = memberService.queryMemberGradeByParams(params);

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", response.getData());
        result.put("userGradeList", userGradeList);

        return getSuccessResult(result);
    }

    /**
     * 开卡赠礼详情
     * */
    @ApiOperation(value = "开卡赠礼详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('openGift:index')")
    public ResponseObject info(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));

        Map<String, Object> param = new HashMap<>();

        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            param.put("MERCHANT_ID", accountInfo.getMerchantId());
        }
        List<MtUserGrade> userGradeMap = memberService.queryMemberGradeByParams(param);

        OpenGiftDto openGiftInfo = openGiftService.getOpenGiftDetail(id);

        Map<String, Object> result = new HashMap<>();
        result.put("openGiftInfo", openGiftInfo);
        result.put("userGradeMap", userGradeMap);

        return getSuccessResult(result);
    }

    /**
     * 提交开卡赠礼
     */
    @ApiOperation(value = "提交开卡赠礼")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('openGift:add')")
    public ResponseObject handleSave(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));

        String id = param.get("id").toString();
        String gradeId = param.get("gradeId").toString();
        String couponId = param.get("couponId").toString();
        String couponNum = param.get("couponNum").toString();
        String point = param.get("point").toString();
        String status = param.get("status") == null ? StatusEnum.ENABLED.getKey() : param.get("status").toString();

        if (StringUtil.isEmpty(couponId) && StringUtil.isEmpty(couponNum) && StringUtil.isEmpty(point)) {
            return getFailureResult(201, "积分和卡券必须填写一项");
        }

        if (StringUtil.isEmpty(gradeId)) {
            return getFailureResult(201, "会员等级不能为空");
        }

        MtOpenGift mtOpenGift = new MtOpenGift();
        if (StringUtil.isNotEmpty(couponId)) {
            mtOpenGift.setCouponId(Integer.parseInt(couponId));
        } else {
            mtOpenGift.setCouponId(0);
        }
        if (StringUtil.isNotEmpty(couponNum)) {
            mtOpenGift.setCouponNum(Integer.parseInt(couponNum));
        } else {
            mtOpenGift.setCouponNum(0);
        }
        if (StringUtil.isNotEmpty(gradeId)) {
            mtOpenGift.setGradeId(Integer.parseInt(gradeId));
        } else {
            mtOpenGift.setGradeId(0);
        }
        if (StringUtil.isNotEmpty(point)) {
            mtOpenGift.setPoint(Integer.parseInt(point));
        } else {
            mtOpenGift.setPoint(0);
        }
        mtOpenGift.setMerchantId(accountInfo.getMerchantId());
        mtOpenGift.setStoreId(0);
        mtOpenGift.setStatus(status);
        mtOpenGift.setOperator(accountInfo.getAccountName());

        if (StringUtil.isNotEmpty(id)) {
            mtOpenGift.setId(Integer.parseInt(id));
            openGiftService.updateOpenGift(mtOpenGift);
        } else {
            openGiftService.addOpenGift(mtOpenGift);
        }

        return getSuccessResult(true);
    }

    /**
     * 更新开卡赠礼
     */
    @ApiOperation(value = "更新开卡赠礼")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('openGift:index')")
    public ResponseObject updateStatus(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));
        Integer id = param.get("id") == null ? 0 : Integer.parseInt(param.get("id").toString());
        String status = param.get("status") == null ? StatusEnum.ENABLED.getKey() : param.get("status").toString();

        OpenGiftDto info = openGiftService.getOpenGiftDetail(id);
        if (info == null) {
            return getFailureResult(201, "开卡赠礼不存在");
        }

        MtOpenGift reqDto = new MtOpenGift();
        reqDto.setId(id);
        reqDto.setStatus(status);
        reqDto.setOperator(accountInfo.getAccountName());

        openGiftService.updateOpenGift(reqDto);
        return getSuccessResult(true);
    }

    /**
     * 删除开卡赠礼
     */
    @ApiOperation(value = "删除开卡赠礼")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('openGift:index')")
    public ResponseObject delete(HttpServletRequest request, @PathVariable("id") Integer id) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(request.getHeader("Access-Token"));
        openGiftService.deleteOpenGift(id, accountInfo.getAccountName());
        return getSuccessResult(true);
    }
}
