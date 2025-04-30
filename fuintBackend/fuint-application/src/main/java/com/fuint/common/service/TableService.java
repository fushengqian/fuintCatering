package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.HangUpDto;
import com.fuint.common.param.TableParam;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtTable;
import com.fuint.framework.exception.BusinessCheckException;
import java.util.List;
import java.util.Map;

/**
 * 桌码业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface TableService extends IService<MtTable> {

    /**
     * 分页查询列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<MtTable> queryTableListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 添加桌码
     *
     * @param  mtTable
     * @throws BusinessCheckException
     * @return
     */
    MtTable addTable(MtTable mtTable) throws BusinessCheckException;

    /**
     * 根据ID获取桌码信息
     *
     * @param  id ID
     * @throws BusinessCheckException
     * @return
     */
    MtTable queryTableById(Integer id) throws BusinessCheckException;

    /**
     * 根据ID获取桌码信息
     *
     * @param storeId 店铺ID
     * @param code 桌码
     * @throws BusinessCheckException
     * @return
     */
    MtTable queryTableByCode(Integer storeId, String code) throws BusinessCheckException;

    /**
     * 根据ID删除桌码
     *
     * @param  id ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     * @return
     */
    void deleteTable(Integer id, String operator) throws BusinessCheckException;

    /**
     * 更新桌码
     * @param  mtTable
     * @throws BusinessCheckException
     * @return
     * */
    MtTable updateTable(MtTable mtTable) throws BusinessCheckException;

    /**
     * 根据条件搜索桌码
     *
     * @param  params 查询参数
     * @throws BusinessCheckException
     * @return
     * */
    List<MtTable> queryTableListByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 获取桌台列表
     *
     @ param tableParam 请求参数
     * @throws BusinessCheckException
     * @return
     * */
    List<HangUpDto> getActiveTableList(TableParam tableParam) throws BusinessCheckException;

}
