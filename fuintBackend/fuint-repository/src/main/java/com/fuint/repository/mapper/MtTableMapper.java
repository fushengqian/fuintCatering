package com.fuint.repository.mapper;

import com.fuint.repository.model.MtTable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 桌码 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtTableMapper extends BaseMapper<MtTable> {

    List<MtTable> getActiveTableList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId);

    MtTable queryTableByTableCode(@Param("storeId") Integer storeId, @Param("code") String code);

    void updateUseStatus(@Param("tableId") Integer tableId, @Param("useStatus") String useStatus, @Param("useTime") String useTime);

}
