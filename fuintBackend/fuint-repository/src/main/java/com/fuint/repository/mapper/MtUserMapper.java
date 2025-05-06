package com.fuint.repository.mapper;

import com.fuint.repository.bean.MemberTopBean;
import com.fuint.repository.model.MtUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 会员个人信息 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtUserMapper extends BaseMapper<MtUser> {

    List<MtUser> queryMemberByMobile(@Param("merchantId") Integer merchantId, @Param("mobile") String mobile);

    List<MtUser> queryMemberByName(@Param("merchantId") Integer merchantId, @Param("name") String name);

    MtUser queryMemberByOpenId(@Param("merchantId") Integer merchantId, @Param("openId") String openId);

    List<MtUser> findMembersByUserNo(@Param("merchantId") Integer merchantId, @Param("userNo") String userNo);

    void updateActiveTime(@Param("userId") Integer userId, @Param("updateTime") Date updateTime);

    void updateUserBalance(@Param("merchantId") Integer merchantId, @Param("userIds") List<Integer> userIds, @Param("amount") BigDecimal amount);

    void resetMobile(@Param("merchantId") Integer merchantId, @Param("mobile") String mobile, @Param("userId") Integer userId);

    Long getUserCount(@Param("merchantId") Integer merchantId);

    Long getStoreUserCount(@Param("storeId") Integer storeId);

    Long getUserCountByTime(@Param("merchantId") Integer merchantId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    Long getStoreUserCountByTime(@Param("storeId") Integer storeId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    List<MemberTopBean> getMemberConsumeTopList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<Integer> getUserIdList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId);

    List<MtUser> searchMembers(@Param("merchantId") Integer merchantId, @Param("keyword") String keyword);

}
