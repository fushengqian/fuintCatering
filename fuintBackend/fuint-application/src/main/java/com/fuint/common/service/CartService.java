package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtCart;
import java.util.List;
import java.util.Map;

/**
 * 购物车业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface CartService extends IService<MtCart> {

    /**
     * 切换购物车给会员
     *
     * @param userId
     * @param cartIds
     * @return
     * */
    Boolean switchCartIds(Integer userId, String cartIds) throws BusinessCheckException;

    /**
     * 保存购物车
     *
     * @param reqDto
     * @param action + or - or =
     * @throws BusinessCheckException
     * @return
     */
    Integer saveCart(MtCart reqDto, String action) throws BusinessCheckException;

    /**
     * 删除购物车
     *
     * @param cartIds 购物车ID
     * @throws BusinessCheckException
     * @return
     */
    void removeCart(String cartIds) throws BusinessCheckException;

    /**
     * 获取购物车
     *
     * @param  tableId 桌台ID
     * @return
     */
    List<MtCart> getCartByTableId(Integer tableId);

    /**
     * 删除购物车
     *
     * @param  tableId 桌台ID
     * @throws BusinessCheckException
     * @return
     */
    void removeCartByTableId(Integer tableId) throws BusinessCheckException;

    /**
     * 清空会员购物车
     *
     * @param userId 会员ID
     * @throws BusinessCheckException
     * @return
     */
    void clearCart(Integer userId) throws BusinessCheckException;

    /**
     * 根据条件查找
     *
     * @param params 查询参数
     * @return
     * */
    List<MtCart> queryCartListByParams(Map<String, Object> params);

    /**
     * 挂单
     *
     * @param  cartId 购物车ID
     * @param  tableId 桌台ID
     * @param  isVisitor 是否游客
     * @return
     */
    MtCart setTableId(Integer cartId, Integer tableId, String isVisitor) throws BusinessCheckException;

    /**
     * 转台
     *
     * @param  tableId 桌台ID
     * @param  turnTableId
     * @return
     */
    void turnTable(Integer tableId, Integer turnTableId);
}
