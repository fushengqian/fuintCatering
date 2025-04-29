package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtTableArea;
import com.fuint.common.service.TableAreaService;
import com.fuint.common.enums.StatusEnum;
import com.fuint.repository.mapper.MtTableAreaMapper;
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
 * 桌码区域服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class TableAreaServiceImpl extends ServiceImpl<MtTableAreaMapper, MtTableArea> implements TableAreaService {

    private static final Logger logger = LoggerFactory.getLogger(TableAreaServiceImpl.class);

    private MtTableAreaMapper mtTableAreaMapper;

    /**
     * 分页查询数据列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<MtTableArea> queryTableAreaListByPagination(PaginationRequest paginationRequest) {
        Page<MtTableArea> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtTableArea> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtTableArea::getStatus, StatusEnum.DISABLE.getKey());

        lambdaQueryWrapper.orderByAsc(MtTableArea::getId);
        List<MtTableArea> dataList = mtTableAreaMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtTableArea> paginationResponse = new PaginationResponse(pageImpl, MtTableArea.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 添加桌码区域
     *
     * @param mtTableArea 桌码区域信息
     * @return
     */
    @Override
    @OperationServiceLog(description = "新增桌码区域")
    public MtTableArea addTableArea(MtTableArea mtTableArea) throws BusinessCheckException {
        mtTableArea.setStatus(StatusEnum.ENABLED.getKey());
        mtTableArea.setUpdateTime(new Date());
        mtTableArea.setCreateTime(new Date());
        Integer id = mtTableAreaMapper.insert(mtTableArea);
        if (id > 0) {
            return mtTableArea;
        } else {
            throw new BusinessCheckException("新增桌码区域数据失败");
        }
    }

    /**
     * 根据ID获桌码区域取息
     *
     * @param id 桌码区域ID
     * @return
     */
    @Override
    public MtTableArea queryTableAreaById(Integer id) {
        return mtTableAreaMapper.selectById(id);
    }

    /**
     * 根据ID删除桌码区域
     *
     * @param id 桌码区域ID
     * @param operator 操作人
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "删除桌码区域")
    public void deleteTableArea(Integer id, String operator) {
        MtTableArea mtTableArea = queryTableAreaById(id);
        if (null == mtTableArea) {
            return;
        }
        mtTableArea.setStatus(StatusEnum.DISABLE.getKey());
        mtTableArea.setUpdateTime(new Date());
        mtTableAreaMapper.updateById(mtTableArea);
    }

    /**
     * 修改桌码区域数据
     *
     * @param mtTableArea
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新桌码区域")
    public MtTableArea updateTableArea(MtTableArea mtTableArea) throws BusinessCheckException {
        mtTableArea = queryTableAreaById(mtTableArea.getId());
        if (mtTableArea == null) {
            throw new BusinessCheckException("该桌码区域状态异常");
        }
        mtTableArea.setUpdateTime(new Date());
        mtTableAreaMapper.updateById(mtTableArea);
        return mtTableArea;
    }

   /**
    * 根据条件搜索桌码区域
    *
    * @param  params 查询参数
    * @throws BusinessCheckException
    * @return
    * */
    @Override
    public List<MtTableArea> queryTableAreaListByParams(Map<String, Object> params) {
        String status =  params.get("status") == null ? StatusEnum.ENABLED.getKey(): params.get("status").toString();
        String storeId =  params.get("storeId") == null ? "" : params.get("storeId").toString();
        String merchantId =  params.get("merchantId") == null ? "" : params.get("merchantId").toString();

        LambdaQueryWrapper<MtTableArea> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtTableArea::getStatus, status);
        }
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtTableArea::getMerchantId, merchantId);
        }
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.and(wq -> wq
                    .eq(MtTableArea::getStoreId, 0)
                    .or()
                    .eq(MtTableArea::getStoreId, storeId));
        }

        lambdaQueryWrapper.orderByAsc(MtTableArea::getId);
        return mtTableAreaMapper.selectList(lambdaQueryWrapper);
    }
}
