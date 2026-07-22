package com.fuint.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuint.repository.model.MtRider;

/**
 * 骑手 Mapper
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtRiderMapper extends BaseMapper<MtRider> {

    /**
     * 根据会员用户ID查询骑手信息
     */
    MtRider findByUserId(Integer userId);

    /**
     * 根据骑手编号查询骑手信息
     */
    MtRider findByRiderNo(String riderNo);
}
