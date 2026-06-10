package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.member.PointDto;
import com.fuint.common.dto.member.PointRankDto;
import com.fuint.common.param.PointPage;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtPoint;

import java.util.List;

/**
 * 积分业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface PointService extends IService<MtPoint> {

    /**
     * 分页查询积分列表
     *
     * @param pointPage
     * @return
     */
    PaginationResponse<PointDto> queryPointListByPagination(PointPage pointPage);

    /**
     * 添加积分
     *
     * @param  reqPointDto
     * @throws BusinessCheckException
     * @return
     */
    void addPoint(MtPoint reqPointDto) throws BusinessCheckException;

    /**
     * 转赠积分
     *
     * @param userId
     * @param mobile
     * @param amount
     * @param remark
     * @throws BusinessCheckException
     * @return
     */
    boolean doGift(Integer userId, String mobile, Integer amount, String remark) throws BusinessCheckException;

    /**
     * 获取积分排行榜
     *
     * @param merchantId 商户ID
     * @param type 排行类型：total/month/week
     * @return
     */
    List<PointRankDto> getPointRankList(Integer merchantId, String type);
}
