package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.repository.model.MtAddress;
import com.fuint.framework.exception.BusinessCheckException;
import java.util.List;
import java.util.Map;

/**
 * 收货地址业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface AddressService extends IService<MtAddress> {

    /**
     * 保存收货地址
     *
     * @param  mtAddress
     * @throws BusinessCheckException
     * @return
     */
    MtAddress saveAddress(MtAddress mtAddress) throws BusinessCheckException;

    /**
     * 根据ID获取地址信息
     *
     * @param  id 地址ID
     * @throws BusinessCheckException
     * @return
     */
    MtAddress detail(Integer id) throws BusinessCheckException;

    /**
     * 根据条件查询地址列表
     *
     * @param params 查询参数
     * @return
     * */
    List<MtAddress> queryListByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 获取会员默认地址
     *
     * @param  userId 会员ID
     * @throws BusinessCheckException
     * @return
     */
    MtAddress getDefaultAddress(Integer userId) throws BusinessCheckException;

}
