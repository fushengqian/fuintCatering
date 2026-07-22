package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.order.OrderGoodsDto;
import com.fuint.common.dto.order.OrderUserDto;
import com.fuint.common.dto.rider.RiderOrderDto;
import com.fuint.common.dto.rider.RiderStatsDto;
import com.fuint.common.enums.OrderStatusEnum;
import com.fuint.common.param.RiderMyOrderParam;
import com.fuint.common.param.RiderOrderSearchParam;
import com.fuint.common.param.RiderOrderPoolParam;
import com.fuint.common.service.RiderOrderService;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.mapper.*;
import com.fuint.repository.model.*;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 骑手订单服务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class RiderOrderServiceImpl extends ServiceImpl<MtRiderOrderMapper, MtRiderOrder> implements RiderOrderService {

    private static final Logger logger = LoggerFactory.getLogger(RiderOrderServiceImpl.class);

    private MtRiderOrderMapper mtRiderOrderMapper;

    private MtOrderMapper mtOrderMapper;

    private MtOrderGoodsMapper mtOrderGoodsMapper;

    private MtOrderAddressMapper mtOrderAddressMapper;

    private MtStoreMapper mtStoreMapper;

    private MtRegionMapper mtRegionMapper;

    private MtGoodsMapper mtGoodsMapper;

    private MtUserMapper mtUserMapper;

    private MtRiderMapper mtRiderMapper;

    @Override
    public PaginationResponse getOrderPool(RiderOrderPoolParam param, Integer storeId) {
        LambdaQueryWrapper<MtOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MtOrder::getStatus, OrderStatusEnum.WAITING_ACCEPT.getKey());
        wrapper.eq(MtOrder::getDeliverMethod, "rider");
        if (storeId != null && storeId > 0) {
            wrapper.eq(MtOrder::getStoreId, storeId);
        }
        wrapper.orderByDesc(MtOrder::getCreateTime);

        com.github.pagehelper.Page pageHelper = com.github.pagehelper.PageHelper.startPage(
                param.getPage(), param.getPageSize());

        List<MtOrder> orderList = mtOrderMapper.selectList(wrapper);
        List<RiderOrderDto> dtoList = orderList.stream()
                .map(this::convertToRiderOrderDto)
                .collect(Collectors.toList());

        Page<RiderOrderDto> pageImpl = new PageImpl<>(dtoList,
                PageRequest.of(param.getPage() - 1, param.getPageSize()), pageHelper.getTotal());
        PaginationResponse<RiderOrderDto> response = new PaginationResponse(pageImpl, RiderOrderDto.class);
        response.setTotalPages(pageHelper.getPages());
        response.setTotalElements(pageHelper.getTotal());
        response.setContent(dtoList);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiderOrderDto acceptOrder(Integer orderId, Integer riderId, Integer merchantId) throws BusinessCheckException {
        MtOrder order = mtOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessCheckException("订单不存在");
        }
        if (!OrderStatusEnum.WAITING_ACCEPT.getKey().equals(order.getStatus())) {
            throw new BusinessCheckException("该订单已被其他骑手接单");
        }

        MtRider rider = mtRiderMapper.selectById(riderId);
        if (rider == null || !"A".equals(rider.getStatus())) {
            throw new BusinessCheckException("骑手状态异常");
        }

        Date now = new Date();

        order.setStatus(OrderStatusEnum.PREPARING.getKey());
        order.setRiderId(riderId);
        order.setUpdateTime(now);
        mtOrderMapper.updateById(order);

        MtRiderOrder riderOrder = new MtRiderOrder();
        riderOrder.setOrderId(orderId);
        riderOrder.setRiderId(riderId);
        riderOrder.setMerchantId(merchantId);
        riderOrder.setStoreId(order.getStoreId());
        riderOrder.setStatus(OrderStatusEnum.PREPARING.getKey());
        riderOrder.setAcceptTime(now);
        riderOrder.setDeliveryFee(order.getDeliveryFee());
        riderOrder.setCreateTime(now);
        riderOrder.setUpdateTime(now);
        this.save(riderOrder);

        return convertToRiderOrderDto(order);
    }

    @Override
    public PaginationResponse getMyOrders(RiderMyOrderParam param, Integer riderId) {
        String status = param.getStatus();
        List<String> statusList = null;
        if (status != null && !status.isEmpty()) {
            statusList = Arrays.asList(status.split(","));
        }

        com.github.pagehelper.Page pageHelper = com.github.pagehelper.PageHelper.startPage(
                param.getPage(), param.getPageSize());

        List<MtRiderOrder> riderOrderList = mtRiderOrderMapper.findByRiderIdAndStatus(riderId, statusList);

        List<RiderOrderDto> dtoList = new ArrayList<>();
        for (MtRiderOrder riderOrder : riderOrderList) {
            MtOrder order = mtOrderMapper.selectById(riderOrder.getOrderId());
            if (order != null) {
                RiderOrderDto dto = convertToRiderOrderDto(order);
                if (riderOrder.getAcceptTime() != null) {
                    dto.setAcceptTime(DateUtil.formatDate(riderOrder.getAcceptTime(), "yyyy.MM.dd HH:mm"));
                }
                dtoList.add(dto);
            }
        }

        Page<RiderOrderDto> pageImpl = new PageImpl<>(dtoList,
                PageRequest.of(param.getPage() - 1, param.getPageSize()), pageHelper.getTotal());
        PaginationResponse<RiderOrderDto> response = new PaginationResponse(pageImpl, RiderOrderDto.class);
        response.setTotalPages(pageHelper.getPages());
        response.setTotalElements(pageHelper.getTotal());
        response.setContent(dtoList);
        return response;
    }

    @Override
    public RiderOrderDto getOrderDetail(Integer orderId) throws BusinessCheckException {
        MtOrder order = mtOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessCheckException("订单不存在");
        }
        return convertToRiderOrderDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiderOrderDto confirmPickup(Integer orderId, Integer riderId) throws BusinessCheckException {
        MtOrder order = mtOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessCheckException("订单不存在");
        }
        if (!OrderStatusEnum.WAITING_PICKUP.getKey().equals(order.getStatus())) {
            throw new BusinessCheckException("订单状态不正确，当前状态：" + OrderStatusEnum.getValue(order.getStatus()));
        }
        if (!riderId.equals(order.getRiderId())) {
            throw new BusinessCheckException("无权操作该订单");
        }

        Date now = new Date();
        order.setStatus(OrderStatusEnum.DELIVERING.getKey());
        order.setUpdateTime(now);
        mtOrderMapper.updateById(order);

        LambdaQueryWrapper<MtRiderOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MtRiderOrder::getOrderId, orderId);
        wrapper.eq(MtRiderOrder::getRiderId, riderId);
        MtRiderOrder riderOrder = this.getOne(wrapper);
        if (riderOrder != null) {
            riderOrder.setStatus(OrderStatusEnum.DELIVERING.getKey());
            riderOrder.setPickupTime(now);
            riderOrder.setUpdateTime(now);
            this.updateById(riderOrder);
        }

        return convertToRiderOrderDto(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiderOrderDto confirmDeliver(Integer orderId, Integer riderId) throws BusinessCheckException {
        MtOrder order = mtOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessCheckException("订单不存在");
        }
        if (!OrderStatusEnum.DELIVERING.getKey().equals(order.getStatus())) {
            throw new BusinessCheckException("订单状态不正确，当前状态：" + OrderStatusEnum.getValue(order.getStatus()));
        }
        if (!riderId.equals(order.getRiderId())) {
            throw new BusinessCheckException("无权操作该订单");
        }

        Date now = new Date();
        order.setStatus(OrderStatusEnum.RIDER_DELIVERED.getKey());
        order.setUpdateTime(now);
        mtOrderMapper.updateById(order);

        LambdaQueryWrapper<MtRiderOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MtRiderOrder::getOrderId, orderId);
        wrapper.eq(MtRiderOrder::getRiderId, riderId);
        MtRiderOrder riderOrder = this.getOne(wrapper);
        if (riderOrder != null) {
            riderOrder.setStatus(OrderStatusEnum.RIDER_DELIVERED.getKey());
            riderOrder.setDeliverTime(now);
            riderOrder.setUpdateTime(now);
            this.updateById(riderOrder);
        }

        return convertToRiderOrderDto(order);
    }

    @Override
    public PaginationResponse searchCompletedOrders(RiderOrderSearchParam param, Integer riderId) {
        com.github.pagehelper.Page pageHelper = com.github.pagehelper.PageHelper.startPage(
                param.getPage(), param.getPageSize());

        LambdaQueryWrapper<MtRiderOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MtRiderOrder::getRiderId, riderId);
        wrapper.in(MtRiderOrder::getStatus, Arrays.asList(
                OrderStatusEnum.RIDER_DELIVERED.getKey(),
                OrderStatusEnum.RECEIVED.getKey(),
                OrderStatusEnum.COMPLETE.getKey()));

        if (param.getOrderSn() != null && !param.getOrderSn().isEmpty()) {
            MtOrder searchOrder = mtOrderMapper.findByOrderSn(param.getOrderSn());
            if (searchOrder != null) {
                wrapper.eq(MtRiderOrder::getOrderId, searchOrder.getId());
            }
        }

        if (param.getBeginTime() != null && !param.getBeginTime().isEmpty()) {
            wrapper.ge(MtRiderOrder::getCreateTime, param.getBeginTime());
        }
        if (param.getEndTime() != null && !param.getEndTime().isEmpty()) {
            wrapper.le(MtRiderOrder::getCreateTime, param.getEndTime());
        }

        wrapper.orderByDesc(MtRiderOrder::getCreateTime);

        List<MtRiderOrder> riderOrderList = mtRiderOrderMapper.selectList(wrapper);

        List<RiderOrderDto> dtoList = new ArrayList<>();
        for (MtRiderOrder riderOrder : riderOrderList) {
            MtOrder order = mtOrderMapper.selectById(riderOrder.getOrderId());
            if (order != null) {
                RiderOrderDto dto = convertToRiderOrderDto(order);
                if (riderOrder.getAcceptTime() != null) {
                    dto.setAcceptTime(DateUtil.formatDate(riderOrder.getAcceptTime(), "yyyy.MM.dd HH:mm"));
                }
                if (riderOrder.getPickupTime() != null) {
                    dto.setPickupTime(DateUtil.formatDate(riderOrder.getPickupTime(), "yyyy.MM.dd HH:mm"));
                }
                if (riderOrder.getDeliverTime() != null) {
                    dto.setDeliverTime(DateUtil.formatDate(riderOrder.getDeliverTime(), "yyyy.MM.dd HH:mm"));
                }
                dtoList.add(dto);
            }
        }

        Page<RiderOrderDto> pageImpl = new PageImpl<>(dtoList,
                PageRequest.of(param.getPage() - 1, param.getPageSize()), pageHelper.getTotal());
        PaginationResponse<RiderOrderDto> response = new PaginationResponse(pageImpl, RiderOrderDto.class);
        response.setTotalPages(pageHelper.getPages());
        response.setTotalElements(pageHelper.getTotal());
        response.setContent(dtoList);
        return response;
    }

    @Override
    public RiderStatsDto getRiderStats(Integer riderId, String mode) {
        RiderStatsDto stats = new RiderStatsDto();

        String beginTime = DateUtil.formatDate(DateUtil.addDate(new Date(), -7), "yyyy-MM-dd") + " 00:00:00";
        String endTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd") + " 23:59:59";

        if ("month".equals(mode)) {
            beginTime = DateUtil.formatDate(DateUtil.addDate(new Date(), -30), "yyyy-MM-dd") + " 00:00:00";
        } else if ("today".equals(mode)) {
            beginTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd") + " 00:00:00";
        }

        List<Map<String, Object>> deliveryStats = mtRiderOrderMapper.findDeliveryStats(riderId, beginTime, endTime);
        if (deliveryStats != null && !deliveryStats.isEmpty()) {
            Map<String, Object> stat = deliveryStats.get(0);
            stats.setTotalOrders(Integer.parseInt(stat.get("deliveryCount").toString()));
            stats.setTotalIncome(new BigDecimal(stat.get("totalIncome").toString()));
            stats.setAvgDailyOrders(
                    BigDecimal.valueOf(stats.getTotalOrders())
                            .divide(BigDecimal.valueOf(Math.max(1, Integer.parseInt(stat.get("workDays").toString()))),
                                    2, RoundingMode.HALF_UP)
            );
        } else {
            stats.setTotalOrders(0);
            stats.setTotalIncome(BigDecimal.ZERO);
            stats.setAvgDailyOrders(BigDecimal.ZERO);
        }

        stats.setDailyDeliveryList(mtRiderOrderMapper.findDailyDeliveryCount(riderId, beginTime, endTime));
        stats.setDailyIncomeList(mtRiderOrderMapper.findDailyIncome(riderId, beginTime, endTime));

        return stats;
    }

    @Override
    public Map<String, Object> getTodayOverview(Integer riderId) {
        Map<String, Object> result = new HashMap<>();

        String today = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
        String todayBegin = today + " 00:00:00";
        String todayEnd = today + " 23:59:59";

        LambdaQueryWrapper<MtRiderOrder> todayWrapper = Wrappers.lambdaQuery();
        todayWrapper.eq(MtRiderOrder::getRiderId, riderId);
        todayWrapper.ge(MtRiderOrder::getCreateTime, todayBegin);
        todayWrapper.le(MtRiderOrder::getCreateTime, todayEnd);
        int todayOrders = mtRiderOrderMapper.selectCount(todayWrapper).intValue();

        LambdaQueryWrapper<MtRiderOrder> deliveringWrapper = Wrappers.lambdaQuery();
        deliveringWrapper.eq(MtRiderOrder::getRiderId, riderId);
        deliveringWrapper.eq(MtRiderOrder::getStatus, OrderStatusEnum.DELIVERING.getKey());
        int deliveringOrders = mtRiderOrderMapper.selectCount(deliveringWrapper).intValue();

        LambdaQueryWrapper<MtRiderOrder> completedWrapper = Wrappers.lambdaQuery();
        completedWrapper.eq(MtRiderOrder::getRiderId, riderId);
        completedWrapper.in(MtRiderOrder::getStatus, Arrays.asList(
                OrderStatusEnum.RIDER_DELIVERED.getKey(),
                OrderStatusEnum.RECEIVED.getKey(),
                OrderStatusEnum.COMPLETE.getKey()));
        int completedOrders = mtRiderOrderMapper.selectCount(completedWrapper).intValue();

        result.put("todayOrders", todayOrders);
        result.put("deliveringOrders", deliveringOrders);
        result.put("completedOrders", completedOrders);

        return result;
    }

    /**
     * 将订单转换为骑手订单DTO
     */
    private RiderOrderDto convertToRiderOrderDto(MtOrder order) {
        RiderOrderDto dto = new RiderOrderDto();
        dto.setOrderId(order.getId());
        dto.setOrderSn(order.getOrderSn());
        dto.setStatus(order.getStatus());
        dto.setStatusText(OrderStatusEnum.getValue(order.getStatus()));
        dto.setAmount(order.getAmount());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setPayAmount(order.getPayAmount());
        dto.setRemark(order.getRemark());
        dto.setVerifyCode(order.getVerifyCode());
        if (order.getCreateTime() != null) {
            dto.setCreateTime(DateUtil.formatDate(order.getCreateTime(), "yyyy.MM.dd HH:mm"));
        }
        if (order.getPayTime() != null) {
            dto.setPayTime(DateUtil.formatDate(order.getPayTime(), "yyyy.MM.dd HH:mm"));
        }

        MtUser user = mtUserMapper.selectById(order.getUserId());
        if (user != null) {
            OrderUserDto userDto = new OrderUserDto();
            userDto.setId(user.getId());
            userDto.setName(user.getName());
            userDto.setMobile(user.getMobile());
            userDto.setAvatar(user.getAvatar());
            dto.setUserInfo(userDto);
        }

        LambdaQueryWrapper<MtOrderAddress> addressWrapper = Wrappers.lambdaQuery();
        addressWrapper.eq(MtOrderAddress::getOrderId, order.getId());
        MtOrderAddress address = mtOrderAddressMapper.selectOne(addressWrapper);
        if (address != null) {
            String addrStr = "";
            if (address.getProvinceId() != null && address.getProvinceId() > 0) {
                MtRegion province = mtRegionMapper.selectById(address.getProvinceId());
                if (province != null) {
                    addrStr += province.getName();
                }
            }
            if (address.getCityId() != null && address.getCityId() > 0) {
                MtRegion city = mtRegionMapper.selectById(address.getCityId());
                if (city != null) {
                    addrStr += city.getName();
                }
            }
            if (address.getRegionId() != null && address.getRegionId() > 0) {
                MtRegion region = mtRegionMapper.selectById(address.getRegionId());
                if (region != null) {
                    addrStr += region.getName();
                }
            }
            addrStr += address.getDetail();
            dto.setUserAddress(addrStr);
        }

        MtStore store = mtStoreMapper.selectById(order.getStoreId());
        if (store != null) {
            dto.setStoreInfo(store);
            dto.setStoreAddress(store.getAddress());
        }

        LambdaQueryWrapper<MtOrderGoods> goodsWrapper = Wrappers.lambdaQuery();
        goodsWrapper.eq(MtOrderGoods::getOrderId, order.getId());
        List<MtOrderGoods> goodsList = mtOrderGoodsMapper.selectList(goodsWrapper);
        if (goodsList != null && !goodsList.isEmpty()) {
            dto.setGoods(goodsList.stream().map(g -> {
                OrderGoodsDto goodsDto = new OrderGoodsDto();
                MtGoods goods = mtGoodsMapper.selectById(g.getGoodsId());
                goodsDto.setId(g.getId());
                goodsDto.setGoodsId(g.getGoodsId());
                goodsDto.setSkuId(g.getSkuId());
                goodsDto.setNum(g.getNum());
                goodsDto.setPrice(g.getPrice() != null ? g.getPrice().toString() : "0");
                goodsDto.setDiscount(g.getDiscount() != null ? g.getDiscount().toString() : "0");
                if (goods != null) {
                    goodsDto.setName(goods.getName());
                    goodsDto.setImage(goods.getLogo());
                }
                return goodsDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
