package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.TableOverviewDto;
import com.fuint.common.param.TableParam;
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

        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtTable::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtTable::getStoreId, storeId);
        }

        String code = paginationRequest.getSearchParams().get("code") == null ? "" : paginationRequest.getSearchParams().get("code").toString();
        if (StringUtils.isNotBlank(code)) {
            lambdaQueryWrapper.eq(MtTable::getCode, code);
        }
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtTable::getStatus, status);
        }

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
     * @param  mtTable
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新桌码")
    public MtTable updateTable(MtTable mtTable) throws BusinessCheckException {
        MtTable table = queryTableById(mtTable.getId());
        if (table == null) {
            throw new BusinessCheckException("该桌码状态异常");
        }
        if (mtTable.getCode() != null) {
            table.setCode(mtTable.getCode());
        }
        if (mtTable.getStoreId() != null) {
            table.setStoreId(mtTable.getStoreId());
        }
        if (mtTable.getDescription() != null) {
            table.setDescription(mtTable.getDescription());
        }
        if (mtTable.getMaxPeople() != null) {
            table.setMaxPeople(mtTable.getMaxPeople());
        }
        if (mtTable.getStatus() != null) {
            table.setStatus(mtTable.getStatus());
        }
        if (mtTable.getSort() != null) {
            table.setSort(mtTable.getSort());
        }
        if (mtTable.getOperator() != null) {
            table.setOperator(mtTable.getOperator());
        }
        table.setUpdateTime(new Date());
        mtTableMapper.updateById(table);
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
        String code =  params.get("code") == null ? "" : params.get("code").toString();

        LambdaQueryWrapper<MtTable> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(code)) {
            lambdaQueryWrapper.eq(MtTable::getCode, code);
        }
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtTable::getStatus, status);
        }
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtTable::getMerchantId, merchantId);
        }
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtTable::getStoreId, storeId);
        }

        lambdaQueryWrapper.orderByAsc(MtTable::getSort);
        List<MtTable> dataList = mtTableMapper.selectList(lambdaQueryWrapper);
        return dataList;
    }

    /**
     * 获取桌台概览
     *
     * @param  tableParam 请求参数
     * @throws BusinessCheckException
     * @return
     * */
    @Override
    public List<TableOverviewDto> getTableOverView(TableParam tableParam) throws BusinessCheckException {
        if (tableParam.getStoreId() == null || tableParam.getStoreId() <= 0) {
            throw new BusinessCheckException("店铺ID异常");
        }
        List<TableOverviewDto> dataList = new ArrayList<>();
        return dataList;
    }
}
