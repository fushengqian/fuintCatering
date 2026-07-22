package com.fuint.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuint.repository.model.MtRiderOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 骑手配送记录 Mapper
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtRiderOrderMapper extends BaseMapper<MtRiderOrder> {

    /**
     * 根据骑手ID和状态列表查询配送记录
     */
    List<MtRiderOrder> findByRiderIdAndStatus(@Param("riderId") Integer riderId, @Param("statusList") List<String> statusList);

    /**
     * 骑手配送统计（按日期范围）
     */
    List<Map<String, Object>> findDeliveryStats(@Param("riderId") Integer riderId, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * 骑手收入统计（按日期范围）
     */
    BigDecimal findIncomeStats(@Param("riderId") Integer riderId, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * 每日配送单量统计
     */
    List<Map<String, Object>> findDailyDeliveryCount(@Param("riderId") Integer riderId, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /**
     * 每日收入统计
     */
    List<Map<String, Object>> findDailyIncome(@Param("riderId") Integer riderId, @Param("beginTime") String beginTime, @Param("endTime") String endTime);
}
