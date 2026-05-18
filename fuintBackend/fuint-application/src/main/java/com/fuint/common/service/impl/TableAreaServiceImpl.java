package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.system.AccountInfo;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.param.TableAreaPage;
import com.fuint.common.service.TableAreaService;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.mapper.MtTableAreaMapper;
import com.fuint.repository.model.MtTableArea;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 桌码区域服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class TableAreaServiceImpl extends ServiceImpl<MtTableAreaMapper, MtTableArea> implements TableAreaService {

    private static final Logger logger = LoggerFactory.getLogger(TableAreaServiceImpl.class);

    private MtTableAreaMapper mtTableAreaMapper;

    /**
     * 分页查询数据列表
     *
     * @param tableAreaPage
     * @return
     */
    @Override
    public PaginationResponse<MtTableArea> queryTableAreaListByPagination(TableAreaPage tableAreaPage) {
        Page<MtTableArea> pageHelper = PageHelper.startPage(tableAreaPage.getPage(), tableAreaPage.getPageSize());
        LambdaQueryWrapper<MtTableArea> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtTableArea::getStatus, StatusEnum.DISABLE.getKey());

        String name = tableAreaPage.getName();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtTableArea::getName, name);
        }
        String status = tableAreaPage.getStatus();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.like(MtTableArea::getStatus, status);
        }

        lambdaQueryWrapper.orderByAsc(MtTableArea::getId);
        List<MtTableArea> dataList = mtTableAreaMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(tableAreaPage.getPage(), tableAreaPage.getPageSize());
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
        logger.info("新增桌码区域数据成功，id:{}", id);
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
     * 修改桌码区域数据
     *
     * @param  mtTableArea 桌码区域信息
     * @param  accountInfo 操作人
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新桌码区域")
    public MtTableArea updateTableArea(MtTableArea mtTableArea, AccountInfo accountInfo) throws BusinessCheckException {
        mtTableArea = queryTableAreaById(mtTableArea.getId());
        if (mtTableArea == null) {
            throw new BusinessCheckException("该桌码区域状态异常");
        }
        if (accountInfo.getMerchantId() > 0 && !accountInfo.getMerchantId().equals(mtTableArea.getMerchantId())) {
            throw new BusinessCheckException("不同商户，无操作权限");
        }
        mtTableArea.setUpdateTime(new Date());
        mtTableAreaMapper.updateById(mtTableArea);
        return mtTableArea;
    }

}
