package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.MerchantDto;
import com.fuint.common.dto.MerchantSettingDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.module.merchantApi.request.MerchantSettingParam;
import com.fuint.repository.model.MtMerchant;
import java.util.List;
import java.util.Map;

/**
 * 商户业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MerchantService extends IService<MtMerchant> {

    /**
     * 分页查询商户列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<MerchantDto> queryMerchantListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 保存商户信息
     *
     * @param  mtMerchant
     * @throws BusinessCheckException
     * @return
     */
    MtMerchant saveMerchant(MtMerchant mtMerchant) throws BusinessCheckException;

    /**
     * 根据ID获取商户信息
     *
     * @param  id 商户ID
     * @throws BusinessCheckException
     * @return
     */
    MtMerchant queryMerchantById(Integer id) throws BusinessCheckException;

    /**
     * 根据名称获取商户信息
     *
     * @param  name 商户名称
     * @throws BusinessCheckException
     * @return
     */
    MtMerchant queryMerchantByName(String name) throws BusinessCheckException;

    /**
     * 根据商户号获取商户信息
     *
     * @param  merchantNo 商户号
     * @return
     */
    MtMerchant queryMerchantByNo(String merchantNo);

    /**
     * 根据商户号获取商户ID
     *
     * @param  merchantNo 商户号
     * @return
     */
    Integer getMerchantId(String merchantNo);

    /**
     * 更新商户状态
     *
     * @param id       商户ID
     * @param operator 操作人
     * @param status   状态
     * @throws BusinessCheckException
     * @return
     */
    void updateStatus(Integer id, String operator, String status) throws BusinessCheckException;

    /**
     * 根据条件查询商户
     *
     * @param params 查询参数
     * @return
     * */
    List<MtMerchant> queryMerchantByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 查询我的商户列表
     *
     * @param merchantId 商户ID
     * @param storeId 店铺ID
     * @param status 状态
     * @return
     * */
    List<MtMerchant> getMyMerchantList(Integer merchantId, Integer storeId, String status) throws BusinessCheckException;

    /**
     * 获取商户信息
     *
     * @param merchantId 商户ID
     * @param storeId 店铺ID
     * @return
     * */
    MerchantSettingDto getMerchantSettingInfo(Integer merchantId, Integer storeId) throws BusinessCheckException;

    /**
     * 保存商户设置信息
     *
     * @param params 商户设置项
     * @return
     * */
    MerchantSettingDto saveMerchantSetting(MerchantSettingParam params) throws BusinessCheckException;

}
