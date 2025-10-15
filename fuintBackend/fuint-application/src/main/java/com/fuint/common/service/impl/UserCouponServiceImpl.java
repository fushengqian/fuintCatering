package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.Constants;
import com.fuint.common.config.Message;
import com.fuint.common.dto.CouponDto;
import com.fuint.common.dto.MyCouponDto;
import com.fuint.common.enums.*;
import com.fuint.common.param.CouponReceiveParam;
import com.fuint.common.service.*;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.SeqUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * 会员卡券业务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class UserCouponServiceImpl extends ServiceImpl<MtUserCouponMapper, MtUserCoupon> implements UserCouponService {

    private MtUserCouponMapper mtUserCouponMapper;

    /**
     * 卡券服务接口
     * */
    private CouponService couponService;

    /**
     * 卡券分组服务接口
     * */
    private CouponGroupService couponGroupService;

    /**
     * 会员服务接口
     * */
    private MemberService memberService;

    /**
     * 积分服务接口
     * */
    private PointService pointService;

    /**
     * 卡券核销记录服务接口
     * */
    private ConfirmLogService confirmLogService;

    /**
     * 店铺服务接口
     * */
    private StoreService storeService;

    /**
     * 系统设置服务接口
     * */
    private SettingService settingService;

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 分页查询券列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<MtUserCoupon> queryUserCouponListByPagination(PaginationRequest paginationRequest) {
        LambdaQueryWrapper<MtUserCoupon> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtUserCoupon::getStatus, StatusEnum.DISABLE.getKey());

        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getStatus, status);
        }
        String userCouponId = paginationRequest.getSearchParams().get("userCouponId") == null ? "" : paginationRequest.getSearchParams().get("userCouponId").toString();
        if (StringUtils.isNotBlank(userCouponId)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getId, userCouponId);
        }
        String userId = paginationRequest.getSearchParams().get("userId") == null ? "" : paginationRequest.getSearchParams().get("userId").toString();
        if (StringUtils.isNotBlank(userId)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getUserId, userId);
        }
        String couponId = paginationRequest.getSearchParams().get("couponId") == null ? "" : paginationRequest.getSearchParams().get("couponId").toString();
        if (StringUtils.isNotBlank(couponId)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getCouponId, couponId);
        }
        String code = paginationRequest.getSearchParams().get("code") == null ? "" : paginationRequest.getSearchParams().get("code").toString();
        if (StringUtils.isNotBlank(code)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getCode, code);
        }
        String mobile = paginationRequest.getSearchParams().get("mobile") == null ? "" : paginationRequest.getSearchParams().get("mobile").toString();
        if (StringUtils.isNotBlank(mobile)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getMobile, mobile);
        }

        lambdaQueryWrapper.orderByDesc(MtUserCoupon::getId);
        Page<MtUserCoupon> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        List<MtUserCoupon> dataList = mtUserCouponMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtUserCoupon> paginationResponse = new PaginationResponse(pageImpl, MtUserCoupon.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 领取卡券(优惠券、计次卡)
     *
     * @param receiveParam 领取参数
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCoupon(CouponReceiveParam receiveParam) throws BusinessCheckException {
        Integer couponId = receiveParam.getCouponId() == null ? 0 : receiveParam.getCouponId();
        Integer userId = receiveParam.getUserId() == null ? 0 : receiveParam.getUserId();
        Integer num = receiveParam.getNum() == null ? 1 : receiveParam.getNum();
        String receiveCode = receiveParam.getReceiveCode() == null ? "" : receiveParam.getReceiveCode();
        Integer userCouponId = 0;
        MtCoupon couponInfo = couponService.queryCouponById(couponId);
        if (couponInfo == null) {
            MtUserCoupon userCoupon = mtUserCouponMapper.findByCode(receiveCode);
            if (userCoupon != null) {
                if (userCoupon.getUserId() != null && userCoupon.getUserId() > 0) {
                    if (userCoupon.getUserId().compareTo(userId) == 0) {
                        throw new BusinessCheckException(Message.HAS_COUPON);
                    } else {
                        throw new BusinessCheckException(Message.CODE_ERROR);
                    }
                }
                couponInfo = couponService.queryCouponById(userCoupon.getCouponId());
                userCouponId = userCoupon.getId();
            } else {
                throw new BusinessCheckException(Message.CODE_ERROR_1);
            }
            if (couponInfo == null) {
                throw new BusinessCheckException(Message.COUPON_NOT_EXIST);
            }
        }

        if (!couponInfo.getStatus().equals(StatusEnum.ENABLED.getKey())) {
            throw new BusinessCheckException(Message.COUPON_NOT_EXIST);
        }

        MtCouponGroup groupInfo = couponGroupService.queryCouponGroupById(couponInfo.getGroupId());
        MtUser userInfo = memberService.queryMemberById(userId);
        if (null == userInfo) {
            throw new BusinessCheckException(Message.USER_NOT_EXIST);
        }

        // 会员等级限制
        if (couponInfo.getGradeIds() != null && StringUtil.isNotEmpty(couponInfo.getGradeIds())) {
            String gradeIds[] = couponInfo.getGradeIds().split(",");
            if (gradeIds.length > 0) {
                boolean isContains = Arrays.asList(gradeIds).contains(userInfo.getGradeId()+"");
                if (!isContains) {
                    throw new BusinessCheckException(Message.GRADE_ERROR);
                }
            }
        }

        // 是否需要领取码
        if (couponInfo.getReceiveCode() != null && StringUtil.isNotEmpty(couponInfo.getReceiveCode())) {
            if (StringUtil.isEmpty(receiveCode)) {
                throw new BusinessCheckException(Message.NEED_CODE);
            }
            // 线下发放的领取码
            if (couponInfo.getSendWay().equals(SendWayEnum.OFFLINE.getKey())) {
                MtUserCoupon userCoupon = mtUserCouponMapper.findByCode(receiveCode);
                if (userCoupon == null || !userCoupon.getCode().equals(receiveCode)) {
                    throw new BusinessCheckException(Message.CODE_ERROR_1);
                } else {
                    userCouponId = userCoupon.getId();
                }
            }
            // 前台领取的领取码
            if (couponInfo.getSendWay().equals(SendWayEnum.FRONT.getKey()) && !receiveCode.equals(couponInfo.getReceiveCode())) {
                throw new BusinessCheckException(Message.CODE_ERROR);
            }
        }

        // 是否已经领取
        List<String> statusList = Arrays.asList(UserCouponStatusEnum.UNUSED.getKey(), UserCouponStatusEnum.USED.getKey(), UserCouponStatusEnum.EXPIRE.getKey());
        List<MtUserCoupon> userCouponData = mtUserCouponMapper.getUserCouponListByCouponId(userId, couponId, statusList);
        if ((userCouponData.size() >= couponInfo.getLimitNum()) && (couponInfo.getLimitNum() > 0)) {
            throw new BusinessCheckException(Message.MAX_COUPON_LIMIT);
        }

        // 积分不足以领取
        if (couponInfo.getPoint() != null && couponInfo.getPoint() > 0) {
            if (userInfo.getPoint() < couponInfo.getPoint()) {
                throw new BusinessCheckException(Message.POINT_LIMIT);
            }
        }

        // 可领取多张，领取序列号
        String uuid = SeqUtil.getRandomNumber(16);
        for (int i = 1; i <= num; i++) {
             MtUserCoupon userCoupon = new MtUserCoupon();
             if (userCouponId > 0) {
                 userCoupon = mtUserCouponMapper.selectById(userCouponId);
             }
             userCoupon.setMerchantId(userInfo.getMerchantId());
             userCoupon.setStoreId(couponInfo.getStoreId());
             userCoupon.setCouponId(couponInfo.getId());
             userCoupon.setType(couponInfo.getType());
             if (couponInfo.getAmount() != null && couponInfo.getAmount().compareTo(new BigDecimal("0")) > 0) {
                userCoupon.setAmount(couponInfo.getAmount());
             }
             userCoupon.setGroupId(groupInfo.getId());
             userCoupon.setMobile(userInfo.getMobile());
             userCoupon.setUserId(userInfo.getId());
             userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getKey());
             userCoupon.setCreateTime(new Date());
             userCoupon.setUpdateTime(new Date());
             userCoupon.setExpireTime(couponInfo.getEndTime());
             if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FLEX.getKey())) {
                 Date expireTime = new Date();
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime(expireTime);
                 calendar.add(Calendar.DATE, couponInfo.getExpireTime());
                 expireTime = calendar.getTime();
                 userCoupon.setExpireTime(expireTime);
             }

             // 16位随机数
             String code = SeqUtil.getRandomNumber(16);
             userCoupon.setCode(code.toString());
             userCoupon.setUuid(uuid.toString());
             if (userCoupon.getId() != null) {
                 mtUserCouponMapper.updateById(userCoupon);
             } else {
                 mtUserCouponMapper.insert(userCoupon);
             }
        }

        // 是否需要扣除相应积分
        if (couponInfo.getPoint() != null && couponInfo.getPoint() > 0) {
            MtPoint reqPointDto = new MtPoint();
            reqPointDto.setUserId(userId);
            reqPointDto.setAmount(-couponInfo.getPoint());
            reqPointDto.setDescription("领取"+ couponInfo.getName() + "扣除" +couponInfo.getPoint() +"积分");
            reqPointDto.setOperator("");
            pointService.addPoint(reqPointDto);
        }

        return true;
    }

    /**
     * 储值卡券
     *
     * @param paramMap 储值参数
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean preStore(Map<String, Object> paramMap) throws BusinessCheckException {
        Integer couponId = paramMap.get("couponId") == null ? 0 : Integer.parseInt(paramMap.get("couponId").toString());
        Integer userId = paramMap.get("userId") == null ? 0 : Integer.parseInt(paramMap.get("userId").toString());
        String param = paramMap.get("param") == null ? "" : paramMap.get("param").toString();
        Integer orderId = paramMap.get("orderId") == null ? 0 : Integer.parseInt(paramMap.get("orderId").toString());

        if (StringUtil.isEmpty(param) || couponId <= 0 || userId <= 0) {
            throw new BusinessCheckException(Message.PARAM_ERROR);
        }

        MtCoupon couponInfo = couponService.queryCouponById(couponId);
        if (couponInfo == null) {
            throw new BusinessCheckException(Message.COUPON_NOT_EXIST);
        }

        MtUser userInfo = memberService.queryMemberById(userId);
        if (userInfo == null) {
            throw new BusinessCheckException(Message.USER_NOT_EXIST);
        }

        String[] paramArr = param.split(",");

        for (int i = 0; i < paramArr.length; i++) {
            String item = paramArr[i];
            if (StringUtil.isNotEmpty(item)) {
                String buyItem = paramArr[i]; // 100_200_1
                String[] buyItemArr = buyItem.split("_");
                if (StringUtil.isNotEmpty(buyItemArr[2])) {
                    Integer numInt = Integer.parseInt(buyItemArr[2]);
                    for (int j = 1; j <= numInt; j++) {
                        if (StringUtil.isNotEmpty(buyItemArr[1])) {
                            preStoreItem(couponInfo, userInfo, orderId, new BigDecimal(buyItemArr[1]));
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * 获取会员卡券列表
     *
     * @param userId 会员ID
     * @param status 状态
     * @return
     * */
    @Override
    public List<MtUserCoupon> getUserCouponList(Integer userId, List<String> status) {
        return mtUserCouponMapper.getUserCouponList(userId, status);
    }

    /**
     * 获取会员卡券列表
     *
     * @param paramMap
     * @throws BusinessCheckException
     * @return
     * */
    @Override
    public ResponseObject getUserCouponList(Map<String, Object> paramMap) throws BusinessCheckException {
        Integer pageNumber = paramMap.get("pageNumber") == null ? Constants.PAGE_NUMBER : Integer.parseInt(paramMap.get("pageNumber").toString());
        Integer pageSize = paramMap.get("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(paramMap.get("pageSize").toString());
        String userId = paramMap.get("userId") == null ? "" : paramMap.get("userId").toString();
        String userNo = paramMap.get("userNo") == null ? "" : paramMap.get("userNo").toString();
        String status =  paramMap.get("status") == null ? "" : paramMap.get("status").toString();
        String type =  paramMap.get("type") == null ? "": paramMap.get("type").toString();
        String mobile = paramMap.get("mobile") == null ? "" : paramMap.get("mobile").toString();
        String merchantId = paramMap.get("merchantId") == null ? "" : paramMap.get("merchantId").toString();
        String storeId = paramMap.get("storeId") == null ? "" : paramMap.get("storeId").toString();
        String couponId = paramMap.get("couponId") == null ? "" : paramMap.get("couponId").toString();
        String code = paramMap.get("code") == null ? "" : paramMap.get("code").toString();
        String id = paramMap.get("id") == null ? "" : paramMap.get("id").toString();

        // 处理已失效
        if (pageNumber <= 1 && StringUtil.isNotEmpty(userId)) {
            List<String> statusList = Arrays.asList(UserCouponStatusEnum.UNUSED.getKey());
            List<MtUserCoupon> data = mtUserCouponMapper.getUserCouponList(Integer.parseInt(userId), statusList);
            for (MtUserCoupon uc : data) {
                 MtCoupon coupon = couponService.queryCouponById(uc.getCouponId());
                 // 已过期
                 if (coupon.getExpireType().equals(CouponExpireTypeEnum.FIX.getKey()) && coupon.getEndTime() != null && coupon.getEndTime().before(new Date())) {
                     uc.setStatus(UserCouponStatusEnum.EXPIRE.getKey());
                     uc.setUpdateTime(new Date());
                     mtUserCouponMapper.updateById(uc);
                 }
                // 已过期
                if (coupon.getExpireType().equals(CouponExpireTypeEnum.FLEX.getKey()) && uc.getExpireTime() != null && uc.getExpireTime().before(new Date())) {
                    uc.setStatus(UserCouponStatusEnum.EXPIRE.getKey());
                    uc.setUpdateTime(new Date());
                    mtUserCouponMapper.updateById(uc);
                }
                 // 已删除
                 if (coupon.getStatus().equals(StatusEnum.DISABLE.getKey())) {
                     uc.setStatus(UserCouponStatusEnum.DISABLE.getKey());
                     uc.setUpdateTime(new Date());
                     mtUserCouponMapper.updateById(uc);
                 }
            }
        }

        LambdaQueryWrapper<MtUserCoupon> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtUserCoupon::getStatus, StatusEnum.DISABLE.getKey());
        if (StringUtil.isNotEmpty(status)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getStatus, status);
        }
        if (StringUtil.isNotEmpty(userId)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getUserId, userId);
        }
        if (StringUtil.isNotEmpty(userNo)) {
            if (StringUtil.isEmpty(merchantId)) {
                merchantId = "0";
            }
            MtUser userInfo = memberService.queryMemberByUserNo(Integer.parseInt(merchantId), userNo);
            if (userInfo != null) {
                lambdaQueryWrapper.eq(MtUserCoupon::getUserId, userInfo.getId());
            }
        }
        if (StringUtil.isNotEmpty(mobile)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getMobile, mobile);
        }
        if (StringUtil.isNotEmpty(type)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getType, type);
        }
        if (StringUtil.isNotEmpty(merchantId)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getMerchantId, merchantId);
        }
        if (StringUtil.isNotEmpty(storeId)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getStoreId, storeId);
        }
        if (StringUtil.isNotEmpty(couponId)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getCouponId, couponId);
        }
        if (StringUtil.isNotEmpty(code)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getCode, code);
        }
        if (StringUtil.isNotEmpty(id)) {
            lambdaQueryWrapper.eq(MtUserCoupon::getId, id);
        }

        lambdaQueryWrapper.orderByDesc(MtUserCoupon::getId);
        Page<MtUserCoupon> pageHelper = PageHelper.startPage(pageNumber, pageSize);
        List<MtUserCoupon> userCouponList = mtUserCouponMapper.selectList(lambdaQueryWrapper);
        List<MyCouponDto> dataList = new ArrayList<>();

        if (userCouponList.size() > 0) {
            for (MtUserCoupon userCouponDto : userCouponList) {
                MtCoupon couponInfo = couponService.queryCouponById(userCouponDto.getCouponId());
                MtUser userInfo = memberService.queryMemberById(userCouponDto.getUserId());
                MtStore storeInfo = storeService.queryStoreById(userCouponDto.getStoreId());
                if (couponInfo == null) {
                    continue;
                }
                if (userInfo != null) {
                    userInfo.setMobile(CommonUtil.hidePhone(userInfo.getMobile()));
                }
                MyCouponDto dto = new MyCouponDto();
                dto.setId(userCouponDto.getId());
                dto.setName(couponInfo.getName());
                dto.setCode(userCouponDto.getCode());
                dto.setCouponId(couponInfo.getId());
                dto.setUseRule(couponInfo.getDescription());
                dto.setContent(couponInfo.getContent());
                String image = couponInfo.getImage();
                String baseImage = settingService.getUploadBasePath();
                dto.setImage(baseImage + image);
                dto.setStatus(userCouponDto.getStatus());
                dto.setAmount(userCouponDto.getAmount());
                dto.setBalance(userCouponDto.getBalance());
                dto.setType(couponInfo.getType());
                dto.setUsedTime(userCouponDto.getUsedTime());
                dto.setCreateTime(userCouponDto.getCreateTime());
                dto.setUserInfo(userInfo);
                dto.setStoreInfo(storeInfo);
                dto.setNum(0);

                // 次卡核销情况
                if (dto.getType().equals(CouponTypeEnum.TIMER.getKey())) {
                    if (StringUtil.isNotEmpty(couponInfo.getOutRule())) {
                        dto.setAmount(new BigDecimal(couponInfo.getOutRule()));
                    }
                    Long confirmCount = confirmLogService.getConfirmNum(dto.getId());
                    dto.setBalance(new BigDecimal(confirmCount));
                }

                boolean canUse = couponService.isCouponEffective(couponInfo, userCouponDto);
                if (!userCouponDto.getStatus().equals(UserCouponStatusEnum.UNUSED.getKey())) {
                    canUse = false;
                }
                dto.setCanUse(canUse);

                if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FIX.getKey())) {
                    String effectiveDate = DateUtil.formatDate(couponInfo.getBeginTime(), "yyyy.MM.dd HH:mm") + "-" + DateUtil.formatDate(couponInfo.getEndTime(), "yyyy.MM.dd HH:mm");
                    dto.setEffectiveDate(effectiveDate);
                }
                if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FLEX.getKey())) {
                    String effectiveDate = DateUtil.formatDate(userCouponDto.getCreateTime(), "yyyy.MM.dd HH:mm") + "-" + DateUtil.formatDate(userCouponDto.getExpireTime(), "yyyy.MM.dd HH:mm");
                    dto.setEffectiveDate(effectiveDate);
                }

                String tips = "";

                // 优惠券tips
                if (couponInfo.getType().equals(CouponTypeEnum.COUPON.getKey())) {
                    if (StringUtil.isNotEmpty(couponInfo.getOutRule()) && Float.parseFloat(couponInfo.getOutRule()) > 0) {
                        tips = "满" + couponInfo.getOutRule() + "可用";
                    } else {
                        tips = "无门槛券";
                    }
                }

                // 储值卡tips
                if (couponInfo.getType().equals(CouponTypeEnum.PRESTORE.getKey())) {
                    tips = "￥" + userCouponDto .getAmount() + "，余额￥" + userCouponDto.getBalance();
                }

                // 计次卡tips
                if (couponInfo.getType().equals(CouponTypeEnum.TIMER.getKey())) {
                    Long confirmNum = confirmLogService.getConfirmNum(userCouponDto.getId());
                    tips = "已使用"+ confirmNum +"次，可使用" + couponInfo.getOutRule() + "次";
                    dto.setNum(Integer.parseInt(couponInfo.getOutRule()) - confirmNum.intValue());
                }

                dto.setTips(tips);
                dataList.add(dto);
            }
        }

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MyCouponDto> paginationResponse = new PaginationResponse(pageImpl, MyCouponDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        if (dataList.size() == 0) {
            paginationResponse.setTotalPages(0);
            paginationResponse.setTotalElements(0);
        }
        paginationResponse.setContent(dataList);

        return new ResponseObject(200, "查询成功", paginationResponse);
    }

    /**
     * 获取会员可支付使用的卡券
     *
     * @param userId 会员ID
     * @param storeId 使用门店
     * @param useFor 用途
     * @return
     * */
    @Override
    public List<CouponDto> getPayAbleCouponList(Integer userId, Integer storeId, String useFor) throws BusinessCheckException {
        List<String> statusList = Arrays.asList(UserCouponStatusEnum.UNUSED.getKey());
        List<MtUserCoupon> userCouponList = mtUserCouponMapper.getUserCouponList(userId, statusList);
        List<CouponDto> dataList = new ArrayList<>();

        if (userCouponList.size() > 0) {
            for (MtUserCoupon userCoupon : userCouponList) {
                 MtCoupon couponInfo = couponService.queryCouponById(userCoupon.getCouponId());
                 // 适用门店
                 if (storeId != null && storeId > 0 && StringUtil.isNotEmpty(couponInfo.getStoreIds())) {
                     String[] storeIds = couponInfo.getStoreIds().split(",");
                     if (!Arrays.asList(storeIds).contains(storeId.toString())) {
                         continue;
                     }
                 }
                 // 只取专用卡券
                 if (StringUtil.isNotEmpty(useFor) && !couponInfo.getUseFor().equals(useFor)) {
                     continue;
                 }
                 // 不取专用卡券
                 if (StringUtil.isEmpty(useFor) && couponInfo.getUseFor() != null && StringUtil.isNotEmpty(couponInfo.getUseFor())) {
                     continue;
                 }
                 CouponDto couponDto = new CouponDto();
                 couponDto.setId(couponInfo.getId());
                 couponDto.setUserCouponId(userCoupon.getId());
                 couponDto.setName(couponInfo.getName());
                 couponDto.setAmount(userCoupon.getAmount());
                 couponDto.setStatus(UserCouponStatusEnum.UNUSED.getKey());
                 boolean isEffective = couponService.isCouponEffective(couponInfo, userCoupon);
                 // 1.储值卡可用
                 if (isEffective && couponInfo.getType().equals(CouponTypeEnum.PRESTORE.getKey())) {
                     if (userCoupon.getBalance().compareTo(new BigDecimal("0")) > 0) {
                         couponDto.setType(CouponTypeEnum.PRESTORE.getValue());
                         couponDto.setAmount(userCoupon.getBalance());
                         dataList.add(couponDto);
                     }
                 } else if(isEffective && couponInfo.getType().equals(CouponTypeEnum.COUPON.getKey())) {
                     // 2.无门槛的优惠券可用
                     if (StringUtil.isEmpty(couponInfo.getOutRule()) || couponInfo.getOutRule().equals("0")) {
                         couponDto.setType(CouponTypeEnum.COUPON.getValue());
                         dataList.add(couponDto);
                     }
                 }
            }
        }

        return dataList;
    }

    /**
     * 获取会员卡券详情
     *
     * @param userId 会员ID
     * @param couponId 卡券ID
     * @return
     * */
    @Override
    public  List<MtUserCoupon> getUserCouponDetail(Integer userId, Integer couponId) {
        return mtUserCouponMapper.findUserCouponDetail(couponId, userId);
    }

    /**
     * 获取会员卡券详情
     *
     * @param userCouponId 会员卡券ID
     * @return
     * */
    @Override
    public MtUserCoupon getUserCouponDetail(Integer userCouponId) {
        return mtUserCouponMapper.selectById(userCouponId);
    }

    /**
     * 根据过期时间查询会员卡券
     *
     * @param userId 会员ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     * */
    @Override
    public List<MtUserCoupon> getUserCouponListByExpireTime(Integer userId, String status, String startTime, String endTime) {
        return mtUserCouponMapper.getUserCouponListByExpireTime(userId, status, startTime, endTime);
    }

    /**
     * 会员发送卡券
     *
     * @param orderId 订单ID
     * @param couponId 卡券ID
     * @param userId 会员ID
     * @param mobile 手机号
     * @param num 购买数量
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean buyCouponItem(Integer orderId, Integer couponId, Integer userId, String mobile, Double num) throws BusinessCheckException {
        if (num == null || num <= 0) {
            num = 1.0;
        }
        for (int j = 0; j < num; j++) {
            MtCoupon couponInfo = couponService.queryCouponById(couponId);
            MtUserCoupon userCoupon = new MtUserCoupon();
            userCoupon.setMerchantId(couponInfo.getMerchantId());
            userCoupon.setStoreId(couponInfo.getStoreId());
            userCoupon.setCouponId(couponId);
            userCoupon.setType(couponInfo.getType());
            userCoupon.setGroupId(couponInfo.getGroupId());
            userCoupon.setMobile(mobile);
            userCoupon.setUserId(userId);
            userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getKey());
            userCoupon.setCreateTime(new Date());
            userCoupon.setUpdateTime(new Date());
            userCoupon.setExpireTime(couponInfo.getEndTime());
            if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FLEX.getKey())) {
                Date expireTime = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(expireTime);
                c.add(Calendar.DATE, couponInfo.getExpireTime());
                expireTime = c.getTime();
                userCoupon.setExpireTime(expireTime);
            }
            userCoupon.setOrderId(orderId);

            // 如果购买的是储值卡
            if (couponInfo.getType().equals(CouponTypeEnum.PRESTORE.getKey()) && couponInfo.getInRule() != null) {
                String[] paramArr = couponInfo.getInRule().split(","); // 100_200,300_500
                MtOrder orderInfo = orderService.getOrderInfo(orderId);
                if (orderInfo != null) {
                    BigDecimal payAmount = orderInfo.getPayAmount();
                    BigDecimal totalAmount = new BigDecimal(0);
                    if (paramArr.length > 0) {
                        for (int i = 0; i < paramArr.length; i++) {
                            String[] storeItem = paramArr[i].split("_");
                            if (storeItem.length > 0) {
                                BigDecimal amount = new BigDecimal(paramArr[i].split("_")[0]);
                                if (payAmount.compareTo(amount) >= 0) {
                                    totalAmount = new BigDecimal(paramArr[i].split("_")[1]);
                                    payAmount = payAmount.subtract(amount);
                                }
                            }
                        }
                    }
                    couponInfo.setAmount(totalAmount);
                }
            }

            userCoupon.setAmount(couponInfo.getAmount());
            userCoupon.setBalance(couponInfo.getAmount());

            // 16位随机数
            String code = SeqUtil.getRandomNumber(16);
            userCoupon.setCode(code);
            userCoupon.setUuid(code);

            mtUserCouponMapper.insert(userCoupon);
        }
        return true;
    }

    /**
     * 通过卡券ID删除会员卡券
     *
     * @param couponId 卡券ID
     * @return
     * */
    public void removeUserCouponByCouponId(Integer couponId) {
        if (couponId == null || couponId <= 0) {
            return;
        }
        mtUserCouponMapper.removeUserCouponByCouponId(couponId);
    }

    /**
     * 预存单张
     *
     * @param couponInfo 卡券信息
     * @param userInfo 会员信息
     * @return
     * */
    private boolean preStoreItem(MtCoupon couponInfo, MtUser userInfo, Integer orderId, BigDecimal amount) {
        MtUserCoupon userCoupon = new MtUserCoupon();
        userCoupon.setCouponId(couponInfo.getId());
        userCoupon.setType(couponInfo.getType());
        userCoupon.setGroupId(couponInfo.getGroupId());
        userCoupon.setMobile(userInfo.getMobile());
        userCoupon.setMerchantId(couponInfo.getMerchantId());
        userCoupon.setStoreId(couponInfo.getStoreId());
        userCoupon.setUserId(userInfo.getId());
        userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getKey());
        userCoupon.setCreateTime(new Date());
        userCoupon.setUpdateTime(new Date());
        userCoupon.setExpireTime(couponInfo.getEndTime());
        if (couponInfo.getExpireType().equals(CouponExpireTypeEnum.FLEX.getKey())) {
            Date expireTime = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(expireTime);
            c.add(Calendar.DATE, couponInfo.getExpireTime());
            expireTime = c.getTime();
            userCoupon.setExpireTime(expireTime);
        }

        userCoupon.setOrderId(orderId);
        userCoupon.setAmount(amount);
        userCoupon.setBalance(amount);

        // 16位随机数
        String code = SeqUtil.getRandomNumber(16);
        userCoupon.setCode(code);
        userCoupon.setUuid(code);

        mtUserCouponMapper.insert(userCoupon);
        return true;
    }
}
