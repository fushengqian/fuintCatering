package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.Constants;
import com.fuint.common.dto.SettlementDto;
import com.fuint.common.dto.SettlementOrderDto;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.enums.PayStatusEnum;
import com.fuint.common.enums.PayTypeEnum;
import com.fuint.common.enums.SettleStatusEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.param.OrderListParam;
import com.fuint.common.service.MerchantService;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.SettlementService;
import com.fuint.common.util.CommonUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.module.backendApi.request.SettlementRequest;
import com.fuint.repository.mapper.MtSettlementMapper;
import com.fuint.repository.mapper.MtSettlementOrderMapper;
import com.fuint.repository.model.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * 订单结算相关业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private MtSettlementMapper mtSettlementMapper;

    private MtSettlementOrderMapper mtSettlementOrderMapper;

    /**
     * 订单服务接口
     * */
    private OrderService orderService;

    /**
     * 商户服务接口
     * */
    private MerchantService merchantService;

    /**
     * 分页查询结算列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<MtSettlement> querySettlementListByPagination(PaginationRequest paginationRequest) {
        Page<MtBanner> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtSettlement> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtSettlement::getStatus, StatusEnum.DISABLE.getKey());

        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtSettlement::getStatus, status);
        }
        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtSettlement::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtSettlement::getStoreId, storeId);
        }
        String description = paginationRequest.getSearchParams().get("description") == null ? "" : paginationRequest.getSearchParams().get("description").toString();
        if (StringUtils.isNotBlank(description)) {
            lambdaQueryWrapper.like(MtSettlement::getDescription, description);
        }
        lambdaQueryWrapper.orderByDesc(MtSettlement::getId);
        List<MtSettlement> dataList = mtSettlementMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtSettlement> paginationResponse = new PaginationResponse(pageImpl, MtSettlement.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 提交结算
     *
     * @param  requestParam
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "发起结算")
    public Boolean submitSettlement(SettlementRequest requestParam) throws BusinessCheckException {
        OrderListParam orderParam = new OrderListParam();
        orderParam.setMerchantId(requestParam.getMerchantId());
        orderParam.setStoreId(requestParam.getStoreId());
        orderParam.setDataType("paid");
        orderParam.setStartTime(requestParam.getStartTime());
        orderParam.setEndTime(requestParam.getEndTime());
        orderParam.setSettleStatus(SettleStatusEnum.WAIT.getKey());
        List<String> payType = new ArrayList<>();
        payType.add(PayTypeEnum.JSAPI.getKey());
        payType.add(PayTypeEnum.MICROPAY.getKey());
        payType.add(PayTypeEnum.ALISCAN.getKey());
        orderParam.setPayType(payType);

        orderParam.setPage(Constants.PAGE_NUMBER);
        orderParam.setPageSize(Constants.ALL_ROWS);

        PaginationResponse response = orderService.getUserOrderList(orderParam);
        List<UserOrderDto> orderList = response.getContent();
        if (orderList == null || orderList.size() < 1) {
            throw new BusinessCheckException("暂无符合结算条件的订单");
        }

        BigDecimal amount = new BigDecimal("0");
        BigDecimal totalOrderAmount = new BigDecimal("0");
        if (orderList != null && orderList.size() > 0) {
            for (UserOrderDto orderDto : orderList) {
                 amount = amount.add(orderDto.getPayAmount());
                 totalOrderAmount = totalOrderAmount.add(orderDto.getPayAmount());
            }
        }
        MtSettlement mtSettlement = new MtSettlement();
        mtSettlement.setMerchantId(requestParam.getMerchantId());
        mtSettlement.setStoreId(requestParam.getStoreId());
        mtSettlement.setSettlementNo(CommonUtil.createSettlementNo());
        MtMerchant mtMerchant = merchantService.queryMerchantById(requestParam.getMerchantId());
        BigDecimal percent = new BigDecimal("1");
        if (mtMerchant.getSettleRate() != null && mtMerchant.getSettleRate().compareTo(new BigDecimal("0")) > 0) {
            percent = mtMerchant.getSettleRate().divide(new BigDecimal("100"), BigDecimal.ROUND_CEILING, 4);
        }
        mtSettlement.setAmount(amount.multiply(percent));
        mtSettlement.setSettleRate(percent.multiply(new BigDecimal("100")));
        mtSettlement.setTotalOrderAmount(totalOrderAmount);
        mtSettlement.setStatus(StatusEnum.ENABLED.getKey());
        mtSettlement.setOperator(requestParam.getOperator());
        mtSettlement.setCreateTime(new Date());
        mtSettlement.setUpdateTime(new Date());
        mtSettlementMapper.insert(mtSettlement);
        if (orderList != null && orderList.size() > 0) {
            for (UserOrderDto orderDto : orderList) {
                 MtSettlementOrder mtSettlementOrder = new MtSettlementOrder();
                 mtSettlementOrder.setSettlementId(mtSettlement.getId());
                 mtSettlementOrder.setOrderId(orderDto.getId());
                 mtSettlementOrder.setCreateTime(new Date());
                 mtSettlementOrder.setUpdateTime(new Date());
                 mtSettlement.setStatus(StatusEnum.ENABLED.getKey());
                 mtSettlementOrder.setOperator(mtSettlement.getOperator());
                 mtSettlementOrderMapper.insert(mtSettlementOrder);
                 // 把订单设置为已结算
                 MtOrder mtOrder = orderService.getById(orderDto.getId());
                 mtOrder.setSettleStatus(SettleStatusEnum.COMPLETE.getKey());
                 orderService.updateOrder(mtOrder);
            }
        }
        return true;
    }

    /**
     * 结算确认
     *
     * @param  settlementId 结算ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "结算确认")
    public Boolean doConfirm(Integer settlementId, String operator) throws BusinessCheckException {
       MtSettlement mtSettlement = mtSettlementMapper.selectById(settlementId);
       if (mtSettlement == null) {
           throw new BusinessCheckException("结算数据不存在");
       }
       mtSettlement.setStatus(SettleStatusEnum.COMPLETE.getKey());
       mtSettlement.setPayStatus(PayStatusEnum.SUCCESS.getKey());
       mtSettlement.setUpdateTime(new Date());
       mtSettlement.setOperator(operator);
       mtSettlementMapper.updateById(mtSettlement);
       return true;
    }

    /**
     * 获取结算详情
     *
     * @param settlementId 结算ID
     * @param page 当前页码
     * @param pageSize 每页数量
     * @return
     * */
    @Override
    public SettlementDto getSettlementInfo(Integer settlementId, Integer page, Integer pageSize) throws BusinessCheckException {
        MtSettlement mtSettlement = mtSettlementMapper.selectById(settlementId);
        if (mtSettlement == null) {
            throw new BusinessCheckException("结算单不存在");
        }

        SettlementDto settlementDto = new SettlementDto();
        BeanUtils.copyProperties(mtSettlement, settlementDto);

        Page<MtBanner> pageHelper = PageHelper.startPage(page, pageSize);
        LambdaQueryWrapper<MtSettlementOrder> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtSettlementOrder::getStatus, StatusEnum.DISABLE.getKey());
        lambdaQueryWrapper.eq(MtSettlementOrder::getSettlementId, settlementId);
        lambdaQueryWrapper.orderByDesc(MtSettlementOrder::getId);
        List<MtSettlementOrder> dataList = mtSettlementOrderMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<SettlementOrderDto> paginationResponse = new PaginationResponse(pageImpl, SettlementOrderDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());

        List<SettlementOrderDto> orderList = new ArrayList<>();
        if (dataList != null && dataList.size() > 0) {
            for (MtSettlementOrder mtSettlementOrder : dataList) {
                 SettlementOrderDto settlementOrderDto = new SettlementOrderDto();
                 BeanUtils.copyProperties(mtSettlementOrder, settlementOrderDto);
                 UserOrderDto orderDto = orderService.getOrderById(settlementOrderDto.getOrderId());
                 if (orderDto != null) {
                     settlementOrderDto.setOrderInfo(orderDto);
                 }
                 orderList.add(settlementOrderDto);
            }
        }
        paginationResponse.setContent(orderList);

        settlementDto.setOrderList(paginationResponse);
        return settlementDto;
    }
}
