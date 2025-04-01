package com.fuint.module.backendApi.controller;

import com.fuint.common.Constants;
import com.fuint.common.dto.*;
import com.fuint.common.enums.OrderModeEnum;
import com.fuint.common.enums.PlatformTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.PhoneFormatCheckUtils;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收银管理controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-收银台相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/cashier")
public class BackendCashierController extends BaseController {

    /**
     * 后台账户服务接口
     */
    private AccountService accountService;

    /**
     * 商品类别服务接口
     */
    private CateService cateService;

    /**
     * 购物车服务接口
     * */
    private CartService cartService;

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 商品服务接口
     */
    private GoodsService goodsService;

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 系统设置服务接口
     */
    private SettingService settingService;

    /**
     * 会员服务接口
     */
    private MemberService memberService;

    /**
     * 商户接口
     */
    private MerchantService merchantService;

    /**
     * 收银台初始化
     *
     * @param request HttpServletRequest对象
     * @return
     */
    @ApiOperation(value = "收银台初始化")
    @RequestMapping(value = "/init/{userId}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('cashier:index')")
    public ResponseObject init(HttpServletRequest request, @PathVariable("userId") Integer userId) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));
        Integer cateId = request.getParameter("cateId") == null ? 0 : Integer.parseInt(request.getParameter("cateId"));

        AccountInfo accountDto = TokenUtil.getAccountInfoByToken(token);

        TAccount accountInfo = accountService.getAccountInfoById(accountDto.getId());
        Integer storeId = (accountInfo.getStoreId() == null || accountInfo.getStoreId() < 1) ? 0 : accountInfo.getStoreId();
        MtStore storeInfo = null;
        if (storeId == null || storeId < 1) {
            MtMerchant mtMerchant = merchantService.queryMerchantById(accountInfo.getMerchantId());
            if (mtMerchant != null) {
                storeInfo = storeService.getDefaultStore(mtMerchant.getNo());
            }
        } else {
            storeInfo = storeService.queryStoreById(storeId);
        }
        if (storeInfo != null) {
            storeId = storeInfo.getId();
        }
        MtUser memberInfo = null;
        if (userId != null && userId > 0) {
            memberInfo = memberService.queryMemberById(userId);
        }
        Map<String, Object> param = new HashMap<>();
        param.put("status", StatusEnum.ENABLED.getKey());
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            param.put("merchantId", accountInfo.getMerchantId());
        } else {
            param.put("merchantId", 0);
        }
        if (storeId > 0) {
            param.put("storeId", storeId);
        } else {
            param.put("storeId", 0);
        }
        List<MtGoodsCate> cateList = cateService.queryCateListByParams(param);

        param.put("status", StatusEnum.ENABLED.getKey());
        Map<String, Object> goodsData = goodsService.getStoreGoodsList(storeId, "", cateId, page, pageSize);

        String imagePath = settingService.getUploadBasePath();

        Map<String, Object> result = new HashMap<>();
        result.put("imagePath", imagePath);
        result.put("storeInfo", storeInfo);
        result.put("memberInfo", memberInfo);
        result.put("accountInfo", accountDto);
        result.put("goodsList", goodsData.get("goodsList"));
        result.put("totalGoods", goodsData.get("total"));
        result.put("cateList", cateList);

        return getSuccessResult(result);
    }

    /**
     * 查询商品列表
     *
     * @param request
     * @param param
     * @return
     */
    @ApiOperation(value = "查询商品列表")
    @RequestMapping(value = "/searchGoods", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('cashier:index')")
    public ResponseObject searchGoods(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String keyword =  param.get("keyword") == null ? "" : param.get("keyword").toString();

        AccountInfo accountDto = TokenUtil.getAccountInfoByToken(token);

        TAccount accountInfo = accountService.getAccountInfoById(accountDto.getId());
        Integer storeId = accountInfo.getStoreId();

        if (storeId == null || storeId < 1) {
            MtMerchant mtMerchant = merchantService.queryMerchantById(accountInfo.getMerchantId());
            if (mtMerchant != null) {
                MtStore storeInfo = storeService.getDefaultStore(mtMerchant.getNo());
                if (storeInfo != null) {
                    storeId = storeInfo.getId();
                }
            }
        }

        Map<String, Object> goodsData = goodsService.getStoreGoodsList(storeId, keyword, 0, 1, 100);
        return getSuccessResult(goodsData.get("goodsList"));
    }

    /**
     * 获取商品详情
     *
     * @param goodsId
     * @return
     */
    @ApiOperation(value = "获取商品详情")
    @RequestMapping(value = "/getGoodsInfo/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('cashier:index')")
    public ResponseObject getGoodsInfo(@PathVariable("id") Integer goodsId) throws InvocationTargetException, IllegalAccessException {
        GoodsDto goodsInfo = goodsService.getGoodsDetail(goodsId, false);

        Map<String, Object> result = new HashMap<>();
        result.put("goodsInfo", goodsInfo);

        // 商品规格列表
        List<String> specNameArr = new ArrayList<>();
        List<Integer> specIdArr = new ArrayList<>();
        List<GoodsSpecItemDto> specArr = new ArrayList<>();

        // sku列表
        List<GoodsSkuDto> skuArr = new ArrayList<>();
        if (goodsInfo != null) {
            // 处理规格列表
            for (MtGoodsSpec mtGoodsSpec : goodsInfo.getSpecList()) {
                if (!specNameArr.contains(mtGoodsSpec.getName())) {
                    specNameArr.add(mtGoodsSpec.getName());
                    specIdArr.add(mtGoodsSpec.getId());
                }
            }

            for (int i = 0; i < specNameArr.size(); i++) {
                GoodsSpecItemDto item = new GoodsSpecItemDto();
                List<GoodsSpecChildDto> child = new ArrayList<>();
                Integer specId = specIdArr.get(i) == null ? (i + 1) : specIdArr.get(i);
                String name = specNameArr.get(i);
                for (MtGoodsSpec mtGoodsSpec : goodsInfo.getSpecList()) {
                    if (mtGoodsSpec.getName().equals(name)) {
                        GoodsSpecChildDto e = new GoodsSpecChildDto();
                        e.setId(mtGoodsSpec.getId());
                        e.setName(mtGoodsSpec.getValue());
                        e.setChecked(true);
                        child.add(e);
                    }
                }
                item.setId(specId);
                item.setName(name);
                item.setChild(child);
                specArr.add(item);
            }

            // 处理sku列表
            for (MtGoodsSku mtGoodsSku : goodsInfo.getSkuList()) {
                GoodsSkuDto skuDto = new GoodsSkuDto();
                BeanUtils.copyProperties(mtGoodsSku, skuDto);
                List<MtGoodsSpec> specList = new ArrayList<>();
                String[] specIds = skuDto.getSpecIds().split("-");
                for (String specId : specIds) {
                    MtGoodsSpec spec = goodsService.getSpecDetail(Integer.parseInt(specId));
                    if (spec != null) {
                        specList.add(spec);
                    }
                }
                skuDto.setSpecList(specList);
                skuArr.add(skuDto);
            }
        }

        result.put("specList", specArr);
        result.put("skuList", skuArr);

        return getSuccessResult(result);
    }

    /**
     * 搜索会员信息
     */
    @ApiOperation(value = "搜索会员信息")
    @RequestMapping(value = "/getMemberInfo", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('cashier:index')")
    public ResponseObject getMemberInfo(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String keyword = param.get("keyword") == null ? "" : param.get("keyword").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (StringUtil.isEmpty(keyword)) {
            return getFailureResult(201);
        }

        MtUser userInfo = null;
        // 优先通过手机号、会员号、用户名查询，查不到再进行模糊匹配查找
        if (PhoneFormatCheckUtils.isChinaPhoneLegal(keyword)) {
            userInfo = memberService.queryMemberByMobile(accountInfo.getMerchantId(), keyword);
        } else {
            userInfo = memberService.queryMemberByUserNo(accountInfo.getMerchantId(), keyword);
            if (userInfo == null) {
                userInfo = memberService.queryMemberByName(accountInfo.getMerchantId(), keyword);
            }
        }
        if (userInfo == null) {
            List<MtUser> userList = memberService.searchMembers(accountInfo.getMerchantId(), keyword);
            if (userList != null && userList.size() > 0) {
                userInfo = userList.get(0);
            }
        }

        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            if (!accountInfo.getMerchantId().equals(userInfo.getMerchantId())) {
                return getFailureResult(1004);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("memberInfo", userInfo);
        return getSuccessResult(result);
    }

    /**
     * 获取会员信息
     */
    @ApiOperation(value = "获取会员信息")
    @RequestMapping(value = "/getMemberInfoById/{userId}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('cashier:index')")
    public ResponseObject getMemberInfoById(HttpServletRequest request, @PathVariable("userId") String userId) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (StringUtil.isEmpty(userId)) {
            return getFailureResult(201);
        }

        MtUser userInfo = memberService.queryMemberById(Integer.parseInt(userId));
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0 && !accountInfo.getMerchantId().equals(userInfo.getMerchantId())) {
            return getFailureResult(1004);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("memberInfo", userInfo);
        return getSuccessResult(result);
    }

    /**
     * 执行挂单
     */
    @ApiOperation(value = "执行挂单")
    @RequestMapping(value = "/doHangUp", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('cashier:index')")
    public ResponseObject doHangUp(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String cartIds = param.get("cartIds") == null ? "" : param.get("cartIds").toString();
        String hangNo = param.get("hangNo") == null ? "" : param.get("hangNo").toString();
        String userId = param.get("userId") == null ? "" : param.get("userId").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        if (accountInfo.getMerchantId() == null || accountInfo.getMerchantId() <= 0) {
            return getFailureResult(201, "平台账号不能执行该操作");
        }

        String isVisitor = YesOrNoEnum.NO.getKey();
        if (StringUtil.isEmpty(userId)) {
            isVisitor = YesOrNoEnum.YES.getKey();
        }

        if (StringUtil.isNotEmpty(cartIds)) {
            String[] ids = cartIds.split(",");
            if (ids.length > 0) {
                for (int i = 0; i < ids.length; i++) {
                     cartService.setHangNo(Integer.parseInt(ids[i]), hangNo, isVisitor);
                }
            }
        }

        return getSuccessResult(true);
    }

    /**
     * 获取挂单列表
     */
    @ApiOperation(value = "获取挂单列表")
    @RequestMapping(value = "/getHangUpList", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('cashier:index')")
    public ResponseObject getHangUpList(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);

        List<HangUpDto> dataList = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
             String hangNo = "#0" + (i+1);
             Map<String, Object> param = new HashMap<>();
             param.put("hangNo", hangNo);
             param.put("merchantId", accountInfo.getMerchantId());
             param.put("storeId", accountInfo.getStoreId());
             List<MtCart> cartList = cartService.queryCartListByParams(param);
             HangUpDto dto = new HangUpDto();
             dto.setIsEmpty(true);
             if (cartList.size() > 0) {
                 Integer userId = cartList.get(0).getUserId();
                 String isVisitor = cartList.get(0).getIsVisitor();
                 Map<String, Object> cartInfo = orderService.calculateCartGoods(accountInfo.getMerchantId(), userId, cartList, 0, false, PlatformTypeEnum.PC.getCode(), OrderModeEnum.ONESELF.getKey());
                 dto.setNum(Integer.parseInt(cartInfo.get("totalNum").toString()));
                 dto.setAmount(new BigDecimal(cartInfo.get("totalPrice").toString()));
                 if (isVisitor.equals(YesOrNoEnum.NO.getKey())) {
                     MtUser userInfo = memberService.queryMemberById(userId);
                     dto.setMemberInfo(userInfo);
                 }
                 String dateTime = DateUtil.formatDate(cartList.get(0).getUpdateTime(), "yyyy-MM-dd HH:mm:ss");
                 dto.setDateTime(dateTime);
                 dto.setIsEmpty(false);
             }
             dto.setHangNo(hangNo);
             dataList.add(dto);
        }

        return getSuccessResult(dataList);
    }
}
