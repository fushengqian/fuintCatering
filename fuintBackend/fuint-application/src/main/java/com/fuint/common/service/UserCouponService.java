package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.CouponDto;
import com.fuint.common.param.CouponReceiveParam;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtUserCoupon;
import java.util.List;
import java.util.Map;

/**
 * 会员卡券业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface UserCouponService extends IService<MtUserCoupon> {

    /**
     * 分页查询列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<MtUserCoupon> queryUserCouponListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 领取卡券
     *
     * @param couponReceiveParam
     * @return
     * */
    boolean receiveCoupon(CouponReceiveParam couponReceiveParam) throws BusinessCheckException;

    /**
     * 预存卡券
     *
     * @param paramMap
     * @return
     * */
    boolean preStore(Map<String, Object> paramMap) throws BusinessCheckException;

    /**
     * 获取会员卡券列表
     * @param userId
     * @param status
     * @return
     * */
    List<MtUserCoupon> getUserCouponList(Integer userId, List<String> status) throws BusinessCheckException;

    /**
     * 获取用户的卡券
     * @param paramMap 查询参数
     * @throws BusinessCheckException
     * */
    ResponseObject getUserCouponList(Map<String, Object> paramMap) throws BusinessCheckException;

    /**
     * 获取会员可支付用的卡券
     *
     * @param userId 会员ID
     * @param storeId 使用门店
     * @param useFor 用途
     * @return
     * */
    List<CouponDto> getPayAbleCouponList(Integer userId, Integer storeId, String useFor) throws BusinessCheckException;

    /**
     * 获取会员卡券详情
     * @param userId
     * @param couponId
     * */
    List<MtUserCoupon> getUserCouponDetail(Integer userId, Integer couponId) throws BusinessCheckException;

    /**
     * 获取会员卡券详情
     *
     * @param userCouponId
     * @return
     * */
    MtUserCoupon getUserCouponDetail(Integer userCouponId) throws BusinessCheckException;

    /**
     * 根据过期时间查询会员卡券
     *
     * @param userId
     * @param status
     * @param startTime
     * @param endTime
     * @return
     * */
    List<MtUserCoupon> getUserCouponListByExpireTime(Integer userId, String status, String startTime, String endTime) throws BusinessCheckException;

    /**
     * 给会员发送卡券（会员购买）
     *
     * @param orderId 订单ID
     * @param couponId 卡券ID
     * @param userId 会员ID
     * @param mobile 会员手机号
     * @param num 购买数量
     * @return
     * */
    boolean buyCouponItem(Integer orderId, Integer couponId, Integer userId, String mobile, Integer num) throws BusinessCheckException;

    /**
     * 通过卡券ID删除会员卡券
     *
     * @param couponId 卡券ID
     * @return
     * */
    void removeUserCouponByCouponId(Integer couponId);
}
