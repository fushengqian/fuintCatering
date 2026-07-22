package com.fuint.common.service;

import com.fuint.common.dto.rider.RiderDto;
import com.fuint.framework.exception.BusinessCheckException;

/**
 * 骑手服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface RiderService {

    /**
     * 根据会员用户ID获取骑手信息
     */
    RiderDto getRiderByUserId(Integer userId) throws BusinessCheckException;

    /**
     * 根据骑手ID获取骑手信息
     */
    RiderDto getRiderById(Integer riderId) throws BusinessCheckException;

    /**
     * 判断会员是否为骑手
     */
    boolean isRider(Integer userId);

    /**
     * 更新骑手信息
     */
    RiderDto updateRider(RiderDto riderDto) throws BusinessCheckException;
}
