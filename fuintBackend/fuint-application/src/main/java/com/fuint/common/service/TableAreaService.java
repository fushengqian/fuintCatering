package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtTableArea;
import com.fuint.framework.exception.BusinessCheckException;
import java.util.List;
import java.util.Map;

/**
 * 桌码区域业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface TableAreaService extends IService<MtTableArea> {

    /**
     * 分页查询列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<MtTableArea> queryTableAreaListByPagination(PaginationRequest paginationRequest);

    /**
     * 添加桌码区域
     *
     * @param  mtTableArea
     * @throws BusinessCheckException
     * @return
     */
    MtTableArea addTableArea(MtTableArea mtTableArea) throws BusinessCheckException;

    /**
     * 根据ID获取桌码区域信息
     *
     * @param id ID
     * @return
     */
    MtTableArea queryTableAreaById(Integer id);

    /**
     * 根据ID删除桌码区域
     *
     * @param id ID
     * @param operator 操作人
     * @throws BusinessCheckException
     * @return
     */
    void deleteTableArea(Integer id, String operator) throws BusinessCheckException;

    /**
     * 更新桌码区域
     * @param  mtTableArea
     * @throws BusinessCheckException
     * @return
     * */
    MtTableArea updateTableArea(MtTableArea mtTableArea) throws BusinessCheckException;

    /**
     * 根据条件搜索桌码区域
     *
     * @param params 查询参数
     * @return
     * */
    List<MtTableArea> queryTableAreaListByParams(Map<String, Object> params);
}
