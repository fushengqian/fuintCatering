package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtTable;
import com.fuint.common.service.TableService;
import com.fuint.common.enums.StatusEnum;
import com.fuint.repository.mapper.MtTableMapper;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.pagehelper.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 桌码服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class TableServiceImpl extends ServiceImpl<MtTableMapper, MtTable> implements TableService {

    private static final Logger logger = LoggerFactory.getLogger(TableServiceImpl.class);

    private MtTableMapper mtTableMapper;

    /**
     * 分页查询数据列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<MtTable> queryTableListByPagination(PaginationRequest paginationRequest) {
        Page<MtTable> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtTable> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtTable::getStatus, StatusEnum.DISABLE.getKey());

        lambdaQueryWrapper.orderByAsc(MtTable::getSort);
        List<MtTable> dataList = mtTableMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtTable> paginationResponse = new PaginationResponse(pageImpl, MtTable.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 添加桌码
     *
     * @param mtTable 桌码信息
     * @return
     */
    @Override
    @OperationServiceLog(description = "新增桌码")
    public MtTable addTable(MtTable mtTable) throws BusinessCheckException {
        mtTable.setStatus(StatusEnum.ENABLED.getKey());
        mtTable.setUpdateTime(new Date());
        mtTable.setCreateTime(new Date());
        Integer id = mtTableMapper.insert(mtTable);
        if (id > 0) {
            return mtTable;
        } else {
            logger.error("新增桌码数据失败，mtTable：{}", mtTable);
            throw new BusinessCheckException("新增桌码数据失败");
        }
    }

    /**
     * 根据ID获桌码取息
     *
     * @param id 桌码ID
     * @return
     */
    @Override
    public MtTable queryTableById(Integer id) {
        return mtTableMapper.selectById(id);
    }

    /**
     * 根据ID删除桌码
     *
     * @param id 桌码ID
     * @param operator 操作人
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "删除桌码")
    public void deleteTable(Integer id, String operator) {
        MtTable mtTable = queryTableById(id);
        if (null == mtTable) {
            return;
        }
        mtTable.setStatus(StatusEnum.DISABLE.getKey());
        mtTable.setUpdateTime(new Date());
        mtTableMapper.updateById(mtTable);
    }

    /**
     * 修改桌码数据
     *
     * @param mtTable
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新桌码")
    public MtTable updateTable(MtTable mtTable) throws BusinessCheckException {
        mtTable = queryTableById(mtTable.getId());
        if (mtTable == null) {
            throw new BusinessCheckException("该桌码状态异常");
        }
        mtTable.setUpdateTime(new Date());
        mtTableMapper.updateById(mtTable);
        return mtTable;
    }

   /**
    * 根据条件搜索桌码
    *
    * @param params 查询参数
    * @throws BusinessCheckException
    * @return
    * */
    @Override
    public List<MtTable> queryTableListByParams(Map<String, Object> params) {
        String status =  params.get("status") == null ? StatusEnum.ENABLED.getKey(): params.get("status").toString();
        String storeId =  params.get("storeId") == null ? "" : params.get("storeId").toString();
        String merchantId =  params.get("merchantId") == null ? "" : params.get("merchantId").toString();

        LambdaQueryWrapper<MtTable> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtTable::getStatus, status);
        }
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtTable::getMerchantId, merchantId);
        }
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.and(wq -> wq
                    .eq(MtTable::getStoreId, 0)
                    .or()
                    .eq(MtTable::getStoreId, storeId));
        }

        lambdaQueryWrapper.orderByAsc(MtTable::getSort);
        List<MtTable> dataList = mtTableMapper.selectList(lambdaQueryWrapper);
        return dataList;
    }
}
