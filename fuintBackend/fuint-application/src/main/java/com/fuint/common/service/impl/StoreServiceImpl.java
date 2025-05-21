package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.StoreDto;
import com.fuint.common.dto.StoreInfo;
import com.fuint.common.enums.QrCodeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.MerchantService;
import com.fuint.common.service.StoreService;
import com.fuint.common.service.WeixinService;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.bean.StoreDistanceBean;
import com.fuint.repository.mapper.MtMerchantMapper;
import com.fuint.repository.mapper.MtStoreMapper;
import com.fuint.repository.model.MtMerchant;
import com.fuint.repository.model.MtStore;
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
import java.math.BigDecimal;
import java.util.*;

/**
 * 店铺管理业务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class StoreServiceImpl extends ServiceImpl<MtStoreMapper, MtStore> implements StoreService {

    private static final Logger logger = LoggerFactory.getLogger(StoreServiceImpl.class);

    private MtStoreMapper mtStoreMapper;

    private MtMerchantMapper mtMerchantMapper;

    /**
     * 商户接口
     */
    private MerchantService merchantService;

    /**
     * 微信服务接口
     * */
    private WeixinService weixinService;

    /**
     * 分页查询店铺列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<StoreDto> queryStoreListByPagination(PaginationRequest paginationRequest) {
        Page<MtStore> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtStore> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtStore::getStatus, StatusEnum.DISABLE.getKey());

        String name = paginationRequest.getSearchParams().get("name") == null ? "" : paginationRequest.getSearchParams().get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtStore::getName, name);
        }
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtStore::getStatus, status);
        }
        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtStore::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtStore::getId, storeId);
        }

        lambdaQueryWrapper.orderByAsc(MtStore::getStatus).orderByDesc(MtStore::getIsDefault);
        List<MtStore> storeList = mtStoreMapper.selectList(lambdaQueryWrapper);
        List<StoreDto> dataList = new ArrayList<>();

        for (MtStore mtStore : storeList) {
             StoreDto storeDto = new StoreDto();
             BeanUtils.copyProperties(mtStore, storeDto);
             MtMerchant mtMerchant = mtMerchantMapper.selectById(mtStore.getMerchantId());
             if (mtMerchant != null) {
                 storeDto.setMerchantName(mtMerchant.getName());
             }
             dataList.add(storeDto);
        }

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<StoreDto> paginationResponse = new PaginationResponse(pageImpl, StoreDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 保存店铺信息
     *
     * @param  storeDto 店铺信息
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "保存店铺信息")
    public MtStore saveStore(StoreDto storeDto) throws BusinessCheckException {
        MtStore mtStore = new MtStore();

        // 编辑店铺
        if (storeDto.getId() != null) {
            mtStore = queryStoreById(storeDto.getId());
            if (mtStore == null) {
                throw new BusinessCheckException("该店铺不存在");
            }
        }

        mtStore.setName(storeDto.getName());
        mtStore.setLogo(storeDto.getLogo());
        mtStore.setContact(storeDto.getContact());
        mtStore.setOperator(storeDto.getOperator());
        if (storeDto.getWxMchId() != null) {
            mtStore.setWxMchId(storeDto.getWxMchId());
        }
        if (storeDto.getWxApiV2() != null) {
            mtStore.setWxApiV2(storeDto.getWxApiV2());
        }
        if (storeDto.getWxCertPath() != null) {
            mtStore.setWxCertPath(storeDto.getWxCertPath());
        }
        if (storeDto.getAlipayAppId() != null) {
            mtStore.setAlipayAppId(storeDto.getAlipayAppId());
        }
        if (storeDto.getAlipayPrivateKey() != null) {
            mtStore.setAlipayPrivateKey(storeDto.getAlipayPrivateKey());
        }
        if (storeDto.getAlipayPublicKey() != null) {
            mtStore.setAlipayPublicKey(storeDto.getAlipayPublicKey());
        }
        mtStore.setLicense(storeDto.getLicense());
        mtStore.setCreditCode(storeDto.getCreditCode());
        mtStore.setBankName(storeDto.getBankName());
        mtStore.setBankCardName(storeDto.getBankCardName());
        mtStore.setBankCardNo(storeDto.getBankCardNo());
        mtStore.setUpdateTime(new Date());
        if (storeDto.getId() == null) {
            mtStore.setCreateTime(new Date());
        }
        mtStore.setDescription(storeDto.getDescription());
        mtStore.setPhone(storeDto.getPhone());

        if (storeDto.getIsDefault() != null) {
            if (storeDto.getIsDefault().equals(YesOrNoEnum.YES.getKey())) {
                mtStoreMapper.resetDefaultStore(storeDto.getMerchantId());
            }
        }

        mtStore.setIsDefault(storeDto.getIsDefault());
        mtStore.setAddress(storeDto.getAddress());
        mtStore.setHours(storeDto.getHours());
        mtStore.setLatitude(storeDto.getLatitude());
        mtStore.setLongitude(storeDto.getLongitude());
        mtStore.setStatus(storeDto.getStatus());
        if (storeDto.getMerchantId() != null) {
            mtStore.setMerchantId(storeDto.getMerchantId());
        }

        if (mtStore.getStatus() == null) {
            mtStore.setStatus(StatusEnum.ENABLED.getKey());
        }
        if (mtStore.getId() == null || mtStore.getId() < 1) {
            this.save(mtStore);
        } else {
            mtStoreMapper.updateById(mtStore);
        }

        // 保存二维码
        try {
            String page = QrCodeEnum.STORE.getPage() + "?" + QrCodeEnum.STORE.getKey() + "Id=" + mtStore.getId();
            String qr = weixinService.createQrCode(mtStore.getMerchantId(), QrCodeEnum.STORE.getKey(), mtStore.getId(), page, 320);
            mtStore.setQrCode(qr);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        mtStoreMapper.updateById(mtStore);

        return mtStore;
    }

    /**
     * 根据店铺ID获取店铺信息
     *
     * @param  id 店铺ID
     * @throws BusinessCheckException
     * @return
     */
    @Override
    public MtStore queryStoreById(Integer id) {
        if (id == null || id < 1) {
            return null;
        }
        return mtStoreMapper.selectById(id);
    }

    /**
     * 获取系统默认店铺
     *
     * @param  merchantNo 商户号
     * @return
     */
    @Override
    public MtStore getDefaultStore(String merchantNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", StatusEnum.ENABLED.getKey());
        params.put("is_default", YesOrNoEnum.YES.getKey());
        if (StringUtil.isNotEmpty(merchantNo)) {
            MtMerchant mtMerchant = merchantService.queryMerchantByNo(merchantNo);
            if (mtMerchant != null) {
                params.put("merchantId", mtMerchant.getId());
            }
        }
        List<MtStore> storeList = queryStoresByParams(params);
        if (storeList.size() > 0) {
            return storeList.get(0);
        } else {
            Map<String, Object> param = new HashMap<>();
            param.put("status", StatusEnum.ENABLED.getKey());
            List<MtStore> dataList = queryStoresByParams(param);
            if (dataList.size() > 0) {
                return dataList.get(0);
            } else {
                return null;
            }
        }
    }

    /**
     * 根据店铺名称获取店铺信息
     *
     * @param  storeName 店铺名称
     * @return
     */
    @Override
    public StoreDto queryStoreByName(String storeName) {
        MtStore mtStore = mtStoreMapper.queryStoreByName(storeName);
        StoreDto storeDto = null;

        if (mtStore != null) {
            storeDto = new StoreDto();
            BeanUtils.copyProperties(mtStore, storeDto);
        }

        return storeDto;
    }

    /**
     * 根据店铺ID获取店铺信息
     *
     * @param  id 店铺ID
     * @throws BusinessCheckException
     * @return
     */
    @Override
    public StoreDto queryStoreDtoById(Integer id) throws BusinessCheckException {
        MtStore mtStore = queryStoreById(id);
        if (null == mtStore || StatusEnum.DISABLE.getKey().equals(mtStore.getStatus())) {
            throw new BusinessCheckException("该店铺状态异常");
        }

        StoreDto mtStoreDto = new StoreDto();
        BeanUtils.copyProperties(mtStore, mtStoreDto);

        if (StringUtil.isEmpty(mtStore.getQrCode())) {
            try {
                String page = QrCodeEnum.STORE.getPage() + "?" + QrCodeEnum.STORE.getKey() + "Id = " + mtStore.getId();
                String qr = weixinService.createQrCode(mtStore.getMerchantId(), QrCodeEnum.STORE.getKey(), mtStore.getId(), page, 320);
                mtStoreDto.setQrCode(qr);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        return mtStoreDto;
    }

    /**
     * 更新店铺状态
     *
     * @param  id       店铺ID
     * @param  operator 操作人
     * @param  status   状态
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "修改店铺状态")
    public void updateStatus(Integer id, String operator, String status) throws BusinessCheckException {
        MtStore mtStore = queryStoreById(id);
        if (null == mtStore) {
            throw new BusinessCheckException("该店铺不存在.");
        }

        mtStore.setStatus(status);
        mtStore.setUpdateTime(new Date());
        mtStore.setOperator(operator);

        mtStoreMapper.updateById(mtStore);
    }

    /**
     * 根据条件查询店铺列表
     *
     * @param params 查询参数
     * @return
     * */
    @Override
    public List<MtStore> queryStoresByParams(Map<String, Object> params) {
        LambdaQueryWrapper<MtStore> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtStore::getStatus, StatusEnum.DISABLE.getKey());

        String storeId = params.get("storeId") == null ? "" : params.get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtStore::getId, storeId);
        }

        String name = params.get("name") == null ? "" : params.get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtStore::getName, name);
        }
        String status = params.get("status") == null ? "" : params.get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtStore::getStatus, status);
        }
        String merchantId = params.get("merchantId") == null ? "" : params.get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtStore::getMerchantId, merchantId);
        }

        lambdaQueryWrapper.orderByAsc(MtStore::getStatus).orderByDesc(MtStore::getIsDefault);
        return mtStoreMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 获取可用店铺列表
     *
     * @param merchantId 商户ID
     * @param storeId 店铺ID
     * @return
     * */
    @Override
    public List<MtStore> getActiveStoreList(Integer merchantId, Integer storeId) {
        return mtStoreMapper.getActiveStoreList(merchantId, storeId);
    }

    /**
     * 根据距离远近获取店铺列表
     *
     * @param merchantNo 商户号
     * @param keyword 关键字
     * @param latitude 维度
     * @param longitude 经度
     * @return
     * */
    @Override
    public List<StoreInfo> queryByDistance(String merchantNo, String keyword, String latitude, String longitude) {
        List<StoreInfo> dataList = new ArrayList<>();

        MtMerchant mtMerchant = merchantService.queryMerchantByNo(merchantNo);
        Integer merchantId = (mtMerchant == null) ? 0 : mtMerchant.getId();

        List<StoreDistanceBean> distanceList = mtStoreMapper.queryByDistance(merchantId, keyword, latitude, longitude);
        Map<String, Object> param = new HashMap<>();
        param.put("status", StatusEnum.ENABLED.getKey());
        if (merchantId != null && merchantId > 0) {
            param.put("merchant_id", merchantId);
        }
        List<MtStore> storeList = mtStoreMapper.selectByMap(param);

        if (distanceList != null) {
            for (StoreDistanceBean bean : distanceList) {
                 for (MtStore mtStore : storeList) {
                      if (mtStore.getId().equals(bean.getId())) {
                          if (StringUtil.isNotEmpty(latitude) && StringUtil.isNotEmpty(longitude)) {
                              mtStore.setDistance(new BigDecimal(bean.getDistance()));
                          } else {
                              mtStore.setDistance(new BigDecimal("0.0"));
                          }
                          StoreInfo storeInfo = new StoreInfo();
                          BeanUtils.copyProperties(mtStore, storeInfo);
                          dataList.add(storeInfo);
                      }
                 }
            }
        }

        return dataList;
    }

    /**
     * 获取店铺名称
     *
     * @param storeIds 店铺ID
     * @return
     * */
    @Override
    public String getStoreNames(String storeIds) {
       if (StringUtil.isEmpty(storeIds)) {
           return "";
       }
       String[] ids = storeIds.split(",");
       List<String> storeNames = new ArrayList<>();
       if (ids.length > 0) {
           for (int i = 0; i < ids.length; i++) {
                MtStore mtStore = mtStoreMapper.selectById(Integer.parseInt(ids[i]));
                if (mtStore != null) {
                    storeNames.add(mtStore.getName());
                }
           }
       }
       return String.join(",", storeNames);
    }

    /**
     * 获取店铺名称
     *
     * @param merchantId 商户ID
     * @param storeNames 店铺名称
     * @return
     * */
    @Override
    public String getStoreIds(Integer merchantId, String storeNames) {
        if (StringUtil.isEmpty(storeNames)) {
            return "";
        }
        String[] names = storeNames.split(",");
        List<String> storeIds = new ArrayList<>();
        if (names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                MtStore mtStore = mtStoreMapper.queryStoreByName(names[i]);
                if (mtStore != null) {
                    storeIds.add(mtStore.getId().toString());
                }
            }
        }
        return String.join(",", storeIds);
    }

    /**
     * 根据商户ID删除店铺信息
     *
     * @param merchantId 商户ID
     * @return
     * */
    @Override
    public void deleteStoreByMerchant(Integer merchantId) {
        if (merchantId == null || merchantId <= 0) {
            return;
        }
        mtStoreMapper.deleteStoreByMerchant(merchantId);
    }
}
