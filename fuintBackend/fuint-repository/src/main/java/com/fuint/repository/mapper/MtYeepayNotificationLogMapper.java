package com.fuint.repository.mapper;

import com.fuint.repository.model.MtYeepayNotificationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 易宝支付通知日志表 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtYeepayNotificationLogMapper extends BaseMapper<MtYeepayNotificationLog> {

    /**
     * 根据订单ID查询通知日志
     *
     * @param orderId 订单ID
     * @return 通知日志列表
     */
    List<MtYeepayNotificationLog> findByOrderId(@Param("orderId") String orderId);

    /**
     * 根据处理状态查询通知日志
     *
     * @param processStatus 处理状态
     * @return 通知日志列表
     */
    List<MtYeepayNotificationLog> findByProcessStatus(@Param("processStatus") String processStatus);
}