package com.fuint.common.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.Constants;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.GoodsDto;
import com.fuint.common.dto.GoodsSpecValueDto;
import com.fuint.common.dto.GoodsTopDto;
import com.fuint.common.enums.GoodsTypeEnum;
import com.fuint.common.enums.PlatformTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.SeqUtil;
import com.fuint.common.util.XlsUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.bean.GoodsBean;
import com.fuint.repository.bean.GoodsTopBean;
import com.fuint.repository.mapper.MtGoodsMapper;
import com.fuint.repository.mapper.MtGoodsSkuMapper;
import com.fuint.repository.mapper.MtGoodsSpecMapper;
import com.fuint.repository.mapper.MtStoreGoodsMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品业务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class GoodsServiceImpl extends ServiceImpl<MtGoodsMapper, MtGoods> implements GoodsService {

    private static final Logger logger = LoggerFactory.getLogger(GoodsServiceImpl.class);

    private MtGoodsMapper mtGoodsMapper;

    private MtGoodsSpecMapper mtGoodsSpecMapper;

    private MtGoodsSkuMapper mtGoodsSkuMapper;

    private MtStoreGoodsMapper mtStoreGoodsMapper;

    /**
     * 系统设置服务接口
     * */
    private SettingService settingService;

    /**
     * 商品分类服务接口
     * */
    private CateService cateService;

    /**
     * 店铺服务接口
     * */
    private StoreService storeService;

    /**
     * 卡券服务接口
     * */
    private CouponService couponService;

    /**
     * 分页查询商品列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<GoodsDto> queryGoodsListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException {
        LambdaQueryWrapper<MtGoods> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtGoods::getStatus, StatusEnum.DISABLE.getKey());

        String name = paginationRequest.getSearchParams().get("name") == null ? "" : paginationRequest.getSearchParams().get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtGoods::getName, name);
        }
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtGoods::getStatus, status);
        }
        String goodsNo = paginationRequest.getSearchParams().get("goodsNo") == null ? "" : paginationRequest.getSearchParams().get("goodsNo").toString();
        if (StringUtils.isNotBlank(goodsNo)) {
            lambdaQueryWrapper.eq(MtGoods::getGoodsNo, goodsNo);
        }
        String isSingleSpec = paginationRequest.getSearchParams().get("isSingleSpec") == null ? "" : paginationRequest.getSearchParams().get("isSingleSpec").toString();
        if (StringUtils.isNotBlank(isSingleSpec)) {
            lambdaQueryWrapper.eq(MtGoods::getIsSingleSpec, isSingleSpec);
        }
        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtGoods::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.and(qw -> qw.eq(MtGoods::getStoreId, storeId)
                                           .or(qw2 -> qw2.eq(MtGoods::getStoreId, 0)
                                           .inSql(MtGoods::getId, "SELECT s.GOODS_ID FROM mt_store_goods s WHERE s.STORE_ID = "+ Integer.parseInt(storeId) +" AND s.status = 'A'")));
        }
        String type = paginationRequest.getSearchParams().get("type") == null ? "" : paginationRequest.getSearchParams().get("type").toString();
        if (StringUtils.isNotBlank(type)) {
            lambdaQueryWrapper.eq(MtGoods::getType, type);
        }
        String cateId = paginationRequest.getSearchParams().get("cateId") == null ? "" : paginationRequest.getSearchParams().get("cateId").toString();
        if (StringUtils.isNotBlank(cateId)) {
            lambdaQueryWrapper.eq(MtGoods::getCateId, cateId);
        }
        String hasStock = paginationRequest.getSearchParams().get("stock") == null ? "" : paginationRequest.getSearchParams().get("stock").toString();
        if (StringUtils.isNotBlank(hasStock)) {
            if (hasStock.equals(YesOrNoEnum.YES.getKey())) {
                lambdaQueryWrapper.gt(MtGoods::getStock, 0);
            } else {
                lambdaQueryWrapper.lt(MtGoods::getStock, 1);
            }
        }
        String hasPrice = paginationRequest.getSearchParams().get("hasPrice") == null ? "" : paginationRequest.getSearchParams().get("hasPrice").toString();
        if (StringUtils.isNotBlank(hasPrice)) {
            if (hasPrice.equals(YesOrNoEnum.YES.getKey())) {
                lambdaQueryWrapper.gt(MtGoods::getPrice, 0);
            }
        }
        String platform = paginationRequest.getSearchParams().get("platform") == null ? "" : paginationRequest.getSearchParams().get("platform").toString();
        if (StringUtils.isNotBlank(platform)) {
            if (platform.equals(PlatformTypeEnum.H5.getCode()) || platform.equals(PlatformTypeEnum.MP_WEIXIN.getCode())) {
                // 会员端
                lambdaQueryWrapper.in(MtGoods::getPlatform, new ArrayList<>(Arrays.asList("0", "1")));
            } else if(platform.equals(PlatformTypeEnum.PC.getCode())) {
                // PC端
                lambdaQueryWrapper.in(MtGoods::getPlatform, new ArrayList<>(Arrays.asList("0", "2")));
            } else if(platform.equals("0")) {
                // 不限制
                lambdaQueryWrapper.eq(MtGoods::getPlatform, 0);
            } else if(platform.equals("1")) {
                // 仅会员端
                lambdaQueryWrapper.eq(MtGoods::getPlatform, 1);
            } else if (platform.equals("2")) {
                // 仅PC端
                lambdaQueryWrapper.eq(MtGoods::getPlatform, 2);
            }
        }
        String sortType = paginationRequest.getSearchParams().get("sortType") == null ? "" : paginationRequest.getSearchParams().get("sortType").toString();
        String sortPrice = paginationRequest.getSearchParams().get("sortPrice") == null ? "0" : paginationRequest.getSearchParams().get("sortPrice").toString();
        if (StringUtil.isNotEmpty(sortType)) {
            if (sortType.equals("price")) {
                if (sortPrice.equals("0")) {
                    lambdaQueryWrapper.orderByDesc(MtGoods::getPrice).orderByAsc(MtGoods::getId);
                } else {
                    lambdaQueryWrapper.orderByAsc(MtGoods::getPrice).orderByAsc(MtGoods::getId);
                }
            } else if (sortType.equals("sales")) {
                lambdaQueryWrapper.orderByDesc(MtGoods::getInitSale).orderByAsc(MtGoods::getId);
            } else {
                lambdaQueryWrapper.orderByAsc(MtGoods::getSort).orderByAsc(MtGoods::getId);
            }
        } else {
            lambdaQueryWrapper.orderByAsc(MtGoods::getSort).orderByAsc(MtGoods::getId);
        }
        lambdaQueryWrapper.select(
                MtGoods::getId,
                MtGoods::getGoodsNo,
                MtGoods::getMerchantId,
                MtGoods::getStoreId,
                MtGoods::getName,
                MtGoods::getCanUsePoint,
                MtGoods::getCateId,
                MtGoods::getCostPrice,
                MtGoods::getCouponIds,
                MtGoods::getCreateTime,
                MtGoods::getUpdateTime,
                MtGoods::getInitSale,
                MtGoods::getIsMemberDiscount,
                MtGoods::getIsSingleSpec,
                MtGoods::getPlatform,
                MtGoods::getPrice,
                MtGoods::getLinePrice,
                MtGoods::getImages,
                MtGoods::getLogo,
                MtGoods::getCostPrice,
                MtGoods::getSort,
                MtGoods::getStatus,
                MtGoods::getStock,
                MtGoods::getType,
                MtGoods::getOperator,
                MtGoods::getWeight);
        Page<MtGoods> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        List<MtGoods> goodsList = mtGoodsMapper.selectList(lambdaQueryWrapper);
        List<GoodsDto> dataList = new ArrayList<>();
        String basePath = settingService.getUploadBasePath();
        for (MtGoods mtGoods : goodsList) {
            MtGoodsCate cateInfo = null;
            if (mtGoods.getCateId() != null) {
                cateInfo = cateService.queryCateById(mtGoods.getCateId());
            }
            GoodsDto item = new GoodsDto();
            item.setId(mtGoods.getId());
            item.setInitSale(mtGoods.getInitSale());
            if (StringUtil.isNotEmpty(mtGoods.getLogo())) {
                item.setLogo(basePath + mtGoods.getLogo());
            }
            item.setStoreId(mtGoods.getStoreId());
            if (mtGoods.getStoreId() != null) {
                MtStore storeInfo = storeService.queryStoreById(mtGoods.getStoreId());
                item.setStoreInfo(storeInfo);
            }
            item.setName(mtGoods.getName());
            item.setGoodsNo(mtGoods.getGoodsNo());
            item.setCateId(mtGoods.getCateId());
            item.setStock(mtGoods.getStock());
            item.setCateInfo(cateInfo);
            item.setType(mtGoods.getType());
            item.setPrice(mtGoods.getPrice());
            item.setLinePrice(mtGoods.getLinePrice());
            item.setSalePoint(mtGoods.getSalePoint());
            item.setSort(mtGoods.getSort());
            item.setCreateTime(mtGoods.getCreateTime());
            item.setUpdateTime(mtGoods.getUpdateTime());
            item.setStatus(mtGoods.getStatus());
            item.setOperator(mtGoods.getOperator());
            dataList.add(item);
        }

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse(pageImpl, GoodsDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 保存商品信息
     *
     * @param  reqDto 商品参数
     * @param  storeIds 分配店铺
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "保存商品信息")
    public MtGoods saveGoods(MtGoods reqDto, String storeIds) throws BusinessCheckException {
        MtGoods mtGoods = new MtGoods();
        if (reqDto.getId() > 0) {
            mtGoods = queryGoodsById(reqDto.getId());
            reqDto.setMerchantId(mtGoods.getMerchantId());
        }
        if (reqDto.getMerchantId() != null) {
            mtGoods.setMerchantId(reqDto.getMerchantId() >= 0 ? reqDto.getMerchantId() : 0);
        }
        if (reqDto.getStoreId() != null) {
            mtGoods.setStoreId(reqDto.getStoreId() >= 0 ? reqDto.getStoreId() : 0);
        }
        Integer storeId = reqDto.getStoreId() == null ? 0 : reqDto.getStoreId();
        if (reqDto.getMerchantId() == null || reqDto.getMerchantId() <= 0) {
            MtStore mtStore = storeService.queryStoreById(storeId);
            if (mtStore != null) {
                mtGoods.setMerchantId(mtStore.getMerchantId());
            }
        }
        if (mtGoods.getMerchantId() == null || mtGoods.getMerchantId() < 1) {
            throw new BusinessCheckException("平台方帐号无法执行该操作，请使用商户帐号操作");
        }
        if (StringUtil.isNotEmpty(reqDto.getIsSingleSpec())) {
            mtGoods.setIsSingleSpec(reqDto.getIsSingleSpec());
        }
        if (reqDto.getId() <= 0 && StringUtil.isEmpty(reqDto.getIsSingleSpec())) {
            mtGoods.setIsSingleSpec(YesOrNoEnum.YES.getKey());
        }
        if (StringUtil.isNotEmpty(reqDto.getName())) {
            mtGoods.setName(reqDto.getName());
        }
        if (StringUtil.isNotEmpty(reqDto.getStatus())) {
            mtGoods.setStatus(reqDto.getStatus());
        }
        if (StringUtil.isNotEmpty(reqDto.getLogo())) {
            mtGoods.setLogo(reqDto.getLogo());
        }
        if (StringUtil.isNotEmpty(reqDto.getIsSingleSpec())) {
            mtGoods.setIsSingleSpec(reqDto.getIsSingleSpec());
        }
        if (StringUtil.isNotEmpty(reqDto.getDescription())) {
            mtGoods.setDescription(reqDto.getDescription());
        }
        if (StringUtil.isNotEmpty(reqDto.getOperator())) {
            mtGoods.setOperator(reqDto.getOperator());
        }
        if (StringUtil.isNotEmpty(reqDto.getType())) {
            mtGoods.setType(reqDto.getType());
        }
        if (reqDto.getPlatform() != null) {
            mtGoods.setPlatform(reqDto.getPlatform());
        }
        if (reqDto.getCateId() != null && reqDto.getCateId() > 0) {
            mtGoods.setCateId(reqDto.getCateId());
        }
        if (reqDto.getServiceTime() != null && reqDto.getServiceTime() > 0) {
            mtGoods.setServiceTime(reqDto.getServiceTime());
        }
        if (StringUtil.isNotEmpty(reqDto.getGoodsNo())) {
            mtGoods.setGoodsNo(reqDto.getGoodsNo());
        }
        if (reqDto.getSort() != null) {
            mtGoods.setSort(reqDto.getSort());
        }
        if (reqDto.getId() == null && (mtGoods.getSort().equals("") || mtGoods.getSort() == null )) {
            mtGoods.setSort(0);
        }
        if (reqDto.getPrice() != null) {
            mtGoods.setPrice(reqDto.getPrice());
        }
        if (reqDto.getPrice() == null && reqDto.getId() <= 0) {
            mtGoods.setPrice(new BigDecimal("0.00"));
        }
        if (reqDto.getLinePrice() != null) {
            mtGoods.setLinePrice(reqDto.getLinePrice());
        }
        if (reqDto.getLinePrice() == null && reqDto.getId() <= 0) {
            mtGoods.setLinePrice(new BigDecimal("0.00"));
        }
        if (reqDto.getCostPrice() != null) {
            mtGoods.setCostPrice(reqDto.getCostPrice());
        }
        if (reqDto.getCostPrice() == null && reqDto.getId() <= 0) {
            mtGoods.setCostPrice(new BigDecimal("0.00"));
        }
        if (StringUtil.isNotEmpty(reqDto.getCouponIds())) {
            mtGoods.setCouponIds(reqDto.getCouponIds());
        }
        if (reqDto.getWeight() != null) {
            mtGoods.setWeight(reqDto.getWeight());
        }
        if (reqDto.getInitSale() != null) {
            mtGoods.setInitSale(reqDto.getInitSale());
        }
        if (reqDto.getStock() != null) {
            mtGoods.setStock(reqDto.getStock());
        }
        if (StringUtil.isNotEmpty(reqDto.getSalePoint())) {
            mtGoods.setSalePoint(reqDto.getSalePoint());
        }
        if (StringUtil.isEmpty(reqDto.getSalePoint()) && reqDto.getId() <= 0) {
            reqDto.setSalePoint("");
        }
        if (StringUtil.isNotEmpty(reqDto.getCanUsePoint())) {
            mtGoods.setCanUsePoint(reqDto.getCanUsePoint());
        }
        if (StringUtil.isNotEmpty(reqDto.getIsMemberDiscount())) {
            mtGoods.setIsMemberDiscount(reqDto.getIsMemberDiscount());
        }
        if (StringUtil.isNotEmpty(reqDto.getImages())) {
            mtGoods.setImages(reqDto.getImages());
        }
        if (!mtGoods.getType().equals(GoodsTypeEnum.COUPON.getKey())) {
            mtGoods.setCouponIds("");
        }
        if (mtGoods.getCouponIds() != null && StringUtil.isNotEmpty(mtGoods.getCouponIds())) {
            String str = mtGoods.getCouponIds().replace("，", ",").trim();
            String couponIds[] = str.split(",");
            if (couponIds.length > 0) {
                for (int i = 0; i < couponIds.length; i++) {
                     MtCoupon mtCoupon = couponService.queryCouponById(Integer.parseInt(couponIds[i]));
                     if (mtCoupon == null) {
                         throw new BusinessCheckException("卡券ID等于“"+couponIds[i]+"”的虚拟卡券不存在.");
                     }
                }
            }
        }
        Date dateTime = new Date();
        mtGoods.setUpdateTime(dateTime);
        if (reqDto.getId() == null || reqDto.getId() <= 0) {
            mtGoods.setCreateTime(dateTime);
            this.save(mtGoods);
        } else {
            this.updateById(mtGoods);
        }

        // 维护分配的店铺
        if (StringUtil.isNotEmpty(storeIds)) {
            List<String> storeIdList = Arrays.asList(storeIds.split(",").clone());
            Map<String, Object> param = new HashMap<>();
            param.put("goods_id", mtGoods.getId());
            param.put("status", StatusEnum.ENABLED.getKey());
            List<MtStoreGoods> storeGoodsList = mtStoreGoodsMapper.selectByMap(param);
            // 判断是否有删除
            if (storeGoodsList != null && storeGoodsList.size() > 0) {
                for (MtStoreGoods mtStoreGoods : storeGoodsList) {
                    if (!storeIdList.contains(mtStoreGoods.getStoreId().toString())) {
                        mtStoreGoods.setStatus(StatusEnum.DISABLE.getKey());
                        mtStoreGoods.setUpdateTime(dateTime);
                        mtStoreGoods.setOperator(mtGoods.getOperator());
                        mtStoreGoodsMapper.updateById(mtStoreGoods);
                    }
                }
            }
            // 新增或更新
            if (storeIdList != null && storeIdList.size() > 0) {
                for (String id : storeIdList) {
                    if (StringUtil.isNotEmpty(id) && Integer.parseInt(id) > 0) {
                        MtStoreGoods mtStoreGoods = new MtStoreGoods();
                        mtStoreGoods.setMerchantId(mtGoods.getMerchantId());
                        mtStoreGoods.setStoreId(Integer.parseInt(id));
                        mtStoreGoods.setGoodsId(mtGoods.getId());
                        mtStoreGoods.setCreateTime(dateTime);
                        mtStoreGoods.setUpdateTime(dateTime);
                        mtStoreGoods.setStatus(StatusEnum.ENABLED.getKey());
                        mtStoreGoods.setOperator(reqDto.getOperator());
                        Map<String, Object> params = new HashMap<>();
                        params.put("goods_id", mtGoods.getId());
                        params.put("store_id", id);
                        List<MtStoreGoods> goodsList = mtStoreGoodsMapper.selectByMap(params);
                        if (goodsList != null && goodsList.size() > 0) {
                            mtStoreGoods = goodsList.get(0);
                            mtStoreGoods.setUpdateTime(dateTime);
                            mtStoreGoods.setStatus(StatusEnum.ENABLED.getKey());
                            mtStoreGoods.setOperator(reqDto.getOperator());
                            mtStoreGoodsMapper.updateById(mtStoreGoods);
                        } else {
                            mtStoreGoodsMapper.insert(mtStoreGoods);
                        }
                    }
                }
            }
        }

        return mtGoods;
    }

    /**
     * 根据ID获取商品信息
     *
     * @param  id 商品ID
     * @throws BusinessCheckException
     * @return
     */
    @Override
    public MtGoods queryGoodsById(Integer id) {
       MtGoods mtGoods = mtGoodsMapper.selectById(id);
       if (mtGoods == null) {
           return null;
       }
       return mtGoods;
    }

    /**
     * 根据编码获取商品信息
     *
     * @param  merchantId 商户ID
     * @param  goodsNo 商品编码
     * @throws BusinessCheckException
     * @return
     */
    @Override
    public MtGoods queryGoodsByGoodsNo(Integer merchantId, String goodsNo) {
        return mtGoodsMapper.getByGoodsNo(merchantId, goodsNo);
    }

    /**
     * 根据条码获取sku信息
     *
     * @param  skuNo skuNo
     * @throws BusinessCheckException
     * */
    @Override
    public MtGoodsSku getSkuInfoBySkuNo(String skuNo) {
        List<MtGoodsSku> mtGoodsSkuList = mtGoodsSkuMapper.getBySkuNo(skuNo);
        if (mtGoodsSkuList.size() > 0) {
            return mtGoodsSkuList.get(0);
        }
        return null;
    }

    /**
     * 根据ID获取商品详情
     *
     * @param  id 商品ID
     * @throws BusinessCheckException
     */
    @Override
    public GoodsDto getGoodsDetail(Integer id, boolean getDeleteSpec) {
        if (id == null || id < 1) {
            return null;
        }

        MtGoods mtGoods = mtGoodsMapper.selectById(id);
        GoodsDto goodsInfo = new GoodsDto();

        if (mtGoods != null) {
            try {
                BeanUtils.copyProperties(mtGoods, goodsInfo);
            } catch (Exception e) {
                goodsInfo.setId(mtGoods.getId());
                goodsInfo.setType(mtGoods.getType());
                goodsInfo.setStoreId(mtGoods.getStoreId());
                goodsInfo.setName(mtGoods.getName());
                goodsInfo.setCateId(mtGoods.getCateId());
                goodsInfo.setGoodsNo(mtGoods.getGoodsNo());
                goodsInfo.setIsSingleSpec(mtGoods.getIsSingleSpec());
                goodsInfo.setLogo(mtGoods.getLogo());
                goodsInfo.setImages(mtGoods.getImages());
                goodsInfo.setStatus(mtGoods.getStatus());
                goodsInfo.setSort(mtGoods.getSort());
                goodsInfo.setPrice(mtGoods.getPrice());
                goodsInfo.setLinePrice(mtGoods.getLinePrice());
                goodsInfo.setServiceTime(mtGoods.getServiceTime());
                goodsInfo.setCouponIds(mtGoods.getCouponIds());
            }
        }

        String basePath = settingService.getUploadBasePath();
        if (StringUtil.isNotEmpty(goodsInfo.getLogo())) {
            goodsInfo.setLogo(basePath + goodsInfo.getLogo());
        }

        // 规格列表
        Map<String, Object> param = new HashMap<>();
        param.put("goods_id", id.toString());
        if (getDeleteSpec == false) {
            param.put("status", StatusEnum.ENABLED.getKey());
        }
        List<MtGoodsSpec> goodsSpecList = mtGoodsSpecMapper.selectByMap(param);
        goodsInfo.setSpecList(goodsSpecList);

        // sku列表
        if (goodsInfo.getIsSingleSpec().equals(YesOrNoEnum.NO.getKey())) {
            List<MtGoodsSku> goodsSkuList = mtGoodsSkuMapper.selectByMap(param);
            goodsInfo.setSkuList(goodsSkuList);
            // 多规格商品的价格、库存数量
            if (goodsSkuList.size() > 0) {
                goodsInfo.setPrice(goodsSkuList.get(0).getPrice());
                goodsInfo.setLinePrice(goodsSkuList.get(0).getLinePrice());
                Double stock = 0.0;
                for (MtGoodsSku mtGoodsSku : goodsSkuList) {
                     stock = stock + mtGoodsSku.getStock();
                }
                goodsInfo.setStock(stock);
            } else {
                goodsInfo.setStock(0.0);
            }
        } else {
            goodsInfo.setSkuList(new ArrayList<>());
        }

        // 获取分配的店铺
        String storeIds = getStoreIds(mtGoods.getId());
        goodsInfo.setStoreIds(storeIds);

        return goodsInfo;
    }

    /**
     * 根据ID删除商品信息
     *
     * @param  id ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @OperationServiceLog(description = "删除商品信息")
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Integer id, String operator) throws BusinessCheckException {
        MtGoods cateInfo = queryGoodsById(id);
        if (null == cateInfo) {
            throw new BusinessCheckException("该商品不存在");
        }
        cateInfo.setStatus(StatusEnum.DISABLE.getKey());
        cateInfo.setUpdateTime(new Date());
        cateInfo.setOperator(operator);
        mtGoodsMapper.updateById(cateInfo);
    }

    /**
     * 获取店铺的商品列表
     *
     * @param storeId 店铺ID
     * @param keyword 关键字
     * @param platform 平台
     * @param cateId 分类ID
     * @param page 当前页码
     * @param pageSize 每页页数
     * @throws BusinessCheckException
     * @return
     * */
    @Override
    public Map<String, Object> getStoreGoodsList(Integer storeId, String keyword, String platform, Integer cateId, Integer page, Integer pageSize) throws BusinessCheckException {
        MtStore mtStore = storeService.queryStoreById(storeId);
        if (mtStore == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("goodsList", new ArrayList<>());
            result.put("total", 0);
            return result;
        }
        Integer merchantId = mtStore.getMerchantId() == null ? 0 : mtStore.getMerchantId();
        Page<MtGoods> pageHelper = PageHelper.startPage(page, pageSize);
        List<MtGoods> goodsList = new ArrayList<>();
        List<MtGoodsSku> skuList = new ArrayList<>();
        if (StringUtil.isNotEmpty(keyword)) {
            skuList = mtGoodsSkuMapper.getBySkuNo(keyword);
        }
        if (skuList != null && skuList.size() > 0) {
            MtGoods goods = mtGoodsMapper.selectById(skuList.get(0).getGoodsId());
            goodsList.add(goods);
        } else {
            pageHelper = PageHelper.startPage(page, pageSize);
            if (keyword != null && StringUtil.isNotEmpty(keyword)) {
                goodsList = mtGoodsMapper.searchStoreGoodsList(merchantId, storeId, keyword, platform);
            } else {
                goodsList = mtGoodsMapper.getStoreGoodsList(merchantId, storeId, cateId, platform);
            }
        }
        List<MtGoods> dataList = new ArrayList<>();
        if (goodsList.size() > 0) {
            for (MtGoods mtGoods : goodsList) {
                // 多规格商品价格、库存数量
                if (mtGoods != null && mtGoods.getIsSingleSpec().equals(YesOrNoEnum.NO.getKey())) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("goods_id", mtGoods.getId().toString());
                    param.put("status", StatusEnum.ENABLED.getKey());
                    List<MtGoodsSku> goodsSkuList = mtGoodsSkuMapper.selectByMap(param);
                    if (goodsSkuList.size() > 0) {
                        mtGoods.setPrice(goodsSkuList.get(0).getPrice());
                        mtGoods.setLinePrice(goodsSkuList.get(0).getLinePrice());
                        Double stock = 0.0;
                        for (MtGoodsSku mtGoodsSku : goodsSkuList) {
                             stock = stock + mtGoodsSku.getStock();
                        }
                        mtGoods.setStock(stock);
                    } else {
                        mtGoods.setStock(0.0);
                    }
                }
                dataList.add(mtGoods);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goodsList", dataList);
        data.put("total", pageHelper.getTotal());

        return data;
    }

    /**
     * 通过SKU获取规格列表
     *
     * @param skuId skuID
     * @return
     * */
    @Override
    public List<GoodsSpecValueDto> getSpecListBySkuId(Integer skuId) {
        if (skuId < 0 || skuId == null) {
            return new ArrayList<>();
        }
        List<GoodsSpecValueDto> result = new ArrayList<>();

        MtGoodsSku goodsSku = mtGoodsSkuMapper.selectById(skuId);
        if (goodsSku == null) {
            return result;
        }

        String specIds = goodsSku.getSpecIds();
        String specIdArr[] = specIds.split("-");
        for (String specId : specIdArr) {
            MtGoodsSpec mtGoodsSpec = mtGoodsSpecMapper.selectById(Integer.parseInt(specId));
            if (mtGoodsSpec != null) {
                GoodsSpecValueDto dto = new GoodsSpecValueDto();
                dto.setSpecValueId(mtGoodsSpec.getId());
                dto.setSpecName(mtGoodsSpec.getName());
                dto.setSpecValue(mtGoodsSpec.getValue());
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 获取商品规格详情
     *
     * @param specId 规格ID
     * @return
     * */
    @Override
    public MtGoodsSpec getSpecDetail(Integer specId) {
        return mtGoodsSpecMapper.selectById(specId);
    }

    /**
     * 更新已售数量
     *
     * @param goodsId 商品ID
     * @param saleNum 销售数量
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateInitSale(Integer goodsId, Double saleNum) {
        return mtGoodsMapper.updateInitSale(goodsId, saleNum);
    }

    /**
     * 获取选择商品列表
     *
     * @param params 查询参数
     * @return
     */
    @Override
    public PaginationResponse<GoodsDto> selectGoodsList(Map<String, Object> params) throws BusinessCheckException {
        Integer page = params.get("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(params.get("page").toString());
        Integer pageSize = params.get("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(params.get("pageSize").toString());
        Integer merchantId = (params.get("merchantId") == null || StringUtil.isEmpty(params.get("merchantId").toString())) ? 0 : Integer.parseInt(params.get("merchantId").toString());
        Integer storeId = (params.get("storeId") == null || StringUtil.isEmpty(params.get("storeId").toString())) ? 0 : Integer.parseInt(params.get("storeId").toString());
        Integer cateId = (params.get("cateId") == null || StringUtil.isEmpty(params.get("cateId").toString())) ? 0 : Integer.parseInt(params.get("cateId").toString());
        String keyword = params.get("keyword") == null ? "" : params.get("keyword").toString();

        MtStore mtStore = storeService.queryStoreById(storeId);
        if (mtStore != null && mtStore.getMerchantId() != null) {
            merchantId = mtStore.getMerchantId();
        }
        Page<MtGoods> pageHelper = PageHelper.startPage(page, pageSize);
        List<GoodsDto> dataList = new ArrayList<>();

        List<GoodsBean> goodsList = mtGoodsMapper.selectGoodsList(merchantId, storeId, cateId, keyword);

        for (GoodsBean goodsBean : goodsList) {
             GoodsDto goodsDto = new GoodsDto();
             goodsDto.setId(goodsBean.getGoodsId());
             goodsDto.setLogo(goodsBean.getLogo());
             goodsDto.setName(goodsBean.getName());
             goodsDto.setGoodsNo(goodsBean.getGoodsNo());
             goodsDto.setStoreId(goodsBean.getStoreId());
             goodsDto.setPrice(goodsBean.getPrice());
             goodsDto.setCateId(goodsBean.getCateId());
             goodsDto.setStock(goodsBean.getStock());
             if (goodsBean.getSpecIds() != null) {
                 Map<String, Object> param = new HashMap<>();
                 param.put("GOODS_ID", goodsBean.getGoodsId());
                 param.put("SPEC_IDS", goodsBean.getSpecIds());
                 param.put("STATUS", StatusEnum.ENABLED.getKey());
                 List<MtGoodsSku> goodsSkuList = mtGoodsSkuMapper.selectByMap(param);
                 if (goodsSkuList != null && goodsSkuList.size() > 0) {
                     goodsDto.setSkuId(goodsSkuList.get(0).getId());
                     goodsDto.setPrice(goodsSkuList.get(0).getPrice());
                     if (goodsSkuList.get(0).getLogo() != null && StringUtil.isNotEmpty(goodsSkuList.get(0).getLogo())) {
                         goodsDto.setLogo(goodsSkuList.get(0).getLogo());
                     }
                     goodsDto.setStock(goodsSkuList.get(0).getStock());
                     List<MtGoodsSpec> specList = new ArrayList<>();
                     String[] specIds = goodsBean.getSpecIds().split("-");
                     if (specIds.length > 0) {
                         for (String specId : specIds) {
                              MtGoodsSpec mtGoodsSpec = mtGoodsSpecMapper.selectById(Integer.parseInt(specId));
                              if (mtGoodsSpec != null) {
                                  specList.add(mtGoodsSpec);
                              }
                         }
                     }
                     goodsDto.setSpecList(specList);
                 }
             }
             dataList.add(goodsDto);
        }

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse(pageImpl, GoodsDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 获取商品销售排行榜
     *
     * @param merchantId 商户ID
     * @param storeId 店铺ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     * */
    @Override
    public List<GoodsTopDto> getGoodsSaleTopList(Integer merchantId, Integer storeId, Date startTime, Date endTime) {
        List<GoodsTopBean> dataList = mtGoodsMapper.getGoodsSaleTopList(merchantId, storeId, startTime, endTime);
        List<GoodsTopDto> goodsList = new ArrayList<>();
        if (dataList != null && dataList.size() > 0) {
            for (GoodsTopBean bean : dataList) {
                 GoodsTopDto dto = new GoodsTopDto();
                 BeanUtils.copyProperties(bean, dto);
                 goodsList.add(dto);
            }
        }
        return goodsList;
    }

    /**
     * 获取商品分配的店铺
     *
     * @param goodsId 商品ID
     * @return
     * */
    @Override
    public String getStoreIds(Integer goodsId) {
        if (goodsId == null || goodsId <= 0) {
            return "";
        }
        Map<String, Object> params = new HashMap<>();
        params.put("goods_id", goodsId);
        params.put("status", StatusEnum.ENABLED.getKey());
        List<MtStoreGoods> goodsList = mtStoreGoodsMapper.selectByMap(params);
        List<String> storeIds = new ArrayList<>();
        if (goodsList != null && goodsList.size() > 0) {
            for (MtStoreGoods mtStoreGoods : goodsList) {
                if (!storeIds.contains(mtStoreGoods.getStoreId().toString())) {
                    storeIds.add(mtStoreGoods.getStoreId().toString());
                }
            }
        } else {
            storeIds.add("0");
        }
        return storeIds.stream().collect(Collectors.joining(","));
    }

    /**
     * 导入商品
     *
     * @param file excel文件
     * @param accountInfo 操作者
     * @param filePath 文件路径
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "导入商品列表")
    public Boolean importGoods(MultipartFile file, AccountInfo accountInfo, String filePath) throws BusinessCheckException {
        String originalFileName = file.getOriginalFilename();
        boolean isExcel2003 = XlsUtil.isExcel2003(originalFileName);
        boolean isExcel2007 = XlsUtil.isExcel2007(originalFileName);

        if (!isExcel2003 && !isExcel2007) {
            logger.error("importGoods->uploadFile：{}", "文件类型不正确");
            throw new BusinessCheckException("文件类型不正确");
        }

        if (accountInfo == null || accountInfo.getMerchantId() == null || accountInfo.getMerchantId() <= 0) {
            throw new BusinessCheckException("没有操作权限");
        }

        // 1、录入商品信息
        List<List<String>> goodsList = new ArrayList<>();
        List<List<String>> skuList = new ArrayList<>();
        try {
            goodsList = XlsUtil.readExcelContent(file.getInputStream(), isExcel2003, 0, 1, null, null, null);
            skuList = XlsUtil.readExcelContent(file.getInputStream(), isExcel2003, 1, 1, null, null, null);
        } catch (IOException e) {
            logger.error("GoodsServiceImpl->parseExcelContent{}", e);
            throw new BusinessCheckException("商品导入失败" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (goodsList != null && goodsList.size() > 0) {
            if (goodsList.size() > 1500) {
                throw new BusinessCheckException("商品导入失败，单次导入商品数量不能大于1500");
            }
            for (int i = 0; i < goodsList.size(); i++) {
                List<String> goods = goodsList.get(i);
                MtGoods mtGoods = new MtGoods();
                mtGoods.setId(0);
                mtGoods.setName(goods.get(0));
                mtGoods.setType(GoodsTypeEnum.getKey(goods.get(1)));
                mtGoods.setGoodsNo(StringUtil.isBlank(goods.get(2)) ? SeqUtil.getRandomNumber(15) : goods.get(2));
                mtGoods.setMerchantId(accountInfo.getMerchantId());
                mtGoods.setStoreId(accountInfo.getStoreId());
                Integer cateId = cateService.getGoodsCateId(accountInfo.getMerchantId(), accountInfo.getStoreId(), goods.get(3));
                if ((cateId == null || cateId <= 0) && StringUtil.isNotBlank(goods.get(3))) {
                    MtGoodsCate mtCate = new MtGoodsCate();
                    mtCate.setMerchantId(accountInfo.getMerchantId());
                    mtCate.setStoreId(accountInfo.getStoreId());
                    mtCate.setName(goods.get(3));
                    mtCate.setOperator(accountInfo.getAccountName());
                    mtCate.setDescription("导入商品并创建商品分类");
                    MtGoodsCate mtGoodsCate = cateService.addCate(mtCate);
                    if (mtGoodsCate != null) {
                        cateId = mtGoodsCate.getId();
                    }
                }
                mtGoods.setCateId(cateId);
                mtGoods.setOperator(accountInfo.getAccountName());
                String storeIds = storeService.getStoreIds(accountInfo.getMerchantId(), goods.get(4));
                String images = goods.get(5);
                if (StringUtil.isNotBlank(images)) {
                    String[] imgArr = images.split(",");
                    if (imgArr.length > 0) {
                        mtGoods.setLogo(imgArr[0]);
                        String imagesJson = JSONObject.toJSONString(images.split(","));
                        mtGoods.setImages(imagesJson);
                    }
                } else {
                    mtGoods.setLogo("/static/defaultImage/none.png");
                }
                mtGoods.setSort(Integer.parseInt(goods.get(6)));
                mtGoods.setCanUsePoint(YesOrNoEnum.getKey(goods.get(7)));
                mtGoods.setIsMemberDiscount(YesOrNoEnum.getKey(goods.get(8)));
                if (goods.get(9).equals(YesOrNoEnum.YES.getKey()) || goods.get(9).equals("单规格")) {
                    mtGoods.setIsSingleSpec(YesOrNoEnum.YES.getKey());
                } else {
                    mtGoods.setIsSingleSpec(YesOrNoEnum.NO.getKey());
                }
                mtGoods.setInitSale(Double.parseDouble(goods.get(10)));
                mtGoods.setSalePoint(goods.get(11));
                mtGoods.setDescription(goods.get(12));
                mtGoods.setPrice(new BigDecimal("0"));
                mtGoods.setLinePrice(new BigDecimal("0"));
                mtGoods.setStock(0.0);
                mtGoods.setStatus(StatusEnum.FORBIDDEN.getKey());
                saveGoods(mtGoods, storeIds);
            }
        }

        // 2、录入规格信息
        if (skuList != null && skuList.size() > 0) {
            MtGoods mtGoods = null;
            Double totalStock = 0d;
            BigDecimal price = new BigDecimal("0");
            for (int j = 0; j < skuList.size(); j++) {
                List<String> sku = skuList.get(j);
                List<MtGoods> goodsList1 = mtGoodsMapper.getByGoodsName(accountInfo.getMerchantId(), sku.get(0));
                if (goodsList1.size() == 1) {
                    mtGoods = goodsList1.get(0);
                } else if (goodsList1.size() > 1) {
                    throw new BusinessCheckException("商品导入失败，存在重复商品名称：" + sku.get(0));
                }
                if (mtGoods != null) {
                    // 单规格
                    if (mtGoods.getIsSingleSpec().equals(YesOrNoEnum.YES.getKey())) {
                        mtGoods.setPrice(new BigDecimal(sku.get(4)));
                        mtGoods.setLinePrice(new BigDecimal(sku.get(5)));
                        mtGoods.setStock(Double.parseDouble(sku.get(6)));
                        mtGoods.setWeight(new BigDecimal(sku.get(7)));
                        mtGoodsMapper.updateById(mtGoods);
                    }
                    // 多规格
                    if (mtGoods.getIsSingleSpec().equals(YesOrNoEnum.NO.getKey())) {
                        List<String> specIds = new ArrayList<>();
                        if (StringUtil.isNotEmpty(sku.get(2)) && StringUtil.isNotEmpty(sku.get(3))) {
                            String[] specNameList = sku.get(2).split(",");
                            String[] specValueList = sku.get(3).split(",");
                            if (specNameList.length == specValueList.length) {
                                for (int y = 0; y < specNameList.length; y++) {
                                    Integer specId = getSpecId(mtGoods.getId(), specNameList[y], specValueList[y]);
                                    specIds.add(specId.toString());
                                }
                            }
                        }
                        if (StringUtil.isNotEmpty(sku.get(1))) {
                            MtGoodsSku mtGoodsSku = new MtGoodsSku();
                            mtGoodsSku.setSkuNo(sku.get(1));
                            mtGoodsSku.setGoodsId(mtGoods.getId());
                            mtGoodsSku.setSpecIds(String.join("-", specIds));
                            BigDecimal skuPrice = new BigDecimal(sku.get(4));
                            mtGoodsSku.setPrice(skuPrice);
                            mtGoodsSku.setLinePrice(new BigDecimal(sku.get(5)));
                            Double stock = Double.parseDouble(sku.get(6));
                            mtGoodsSku.setStock(stock);
                            mtGoodsSku.setWeight(new BigDecimal(sku.get(7)));
                            mtGoodsSku.setStatus(StatusEnum.ENABLED.getKey());
                            mtGoodsSkuMapper.insert(mtGoodsSku);
                            totalStock = totalStock + stock;
                            if (((skuPrice.compareTo(price) <= 0) || (price.compareTo(new BigDecimal("0")) <= 0)) && skuPrice.compareTo(new BigDecimal("0")) > 0) {
                                price = skuPrice;
                            }
                        }
                    }
                }
            }
            // 更新商品价格和库存
            if (mtGoods != null) {
                mtGoods.setStock(totalStock);
                mtGoods.setPrice(price);
                mtGoodsMapper.updateById(mtGoods);
            }
        }
        return true;
    }

    /**
     * 获取规格ID
     *
     * @param goodsId 商品ID
     * @param specName 规格名称
     * @param specValue 规格值
     * */
    @Override
    public Integer getSpecId(Integer goodsId, String specName, String specValue) {
        Map<String, Object> params = new HashMap<>();
        params.put("goods_id", goodsId);
        params.put("name", specName);
        params.put("value", specValue);
        params.put("status", StatusEnum.ENABLED.getKey());
        Integer specId;
        List<MtGoodsSpec> specList = mtGoodsSpecMapper.selectByMap(params);
        if (specList != null && specList.size() > 0) {
            specId = specList.get(0).getId();
        } else {
            MtGoodsSpec mtGoodsSpec = new MtGoodsSpec();
            mtGoodsSpec.setGoodsId(goodsId);
            mtGoodsSpec.setName(specName);
            mtGoodsSpec.setValue(specValue);
            mtGoodsSpec.setStatus(StatusEnum.ENABLED.getKey());
            mtGoodsSpecMapper.insert(mtGoodsSpec);
            specId = mtGoodsSpec.getId();
        }
        return specId;
    }
}
