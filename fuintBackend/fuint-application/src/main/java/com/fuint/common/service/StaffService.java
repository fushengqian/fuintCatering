package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.StaffDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtStaff;
import java.util.List;
import java.util.Map;

/**
 * 店铺员工业务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface StaffService extends IService<MtStaff> {

    /**
     * 员工查询列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<MtStaff> queryStaffListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 保存员工信息
     *
     * @param reqStaff 员工信息
     * @param operator 操作人
     * @throws BusinessCheckException
     * @return
     */
    MtStaff saveStaff(MtStaff reqStaff, String operator) throws BusinessCheckException;

    /**
     * 根据ID获取店铺信息
     *
     * @param  id 员工id
     * @throws BusinessCheckException
     */
    MtStaff queryStaffById(Integer id) throws BusinessCheckException;

    /**
     * 审核更改状态(禁用，审核通过)
     *
     * @param  staffId 员工ID
     * @param status 状态
     * @param operator 操作人
     * @throws BusinessCheckException
     * @return
     */
    Integer updateAuditedStatus(Integer staffId, String status, String operator) throws BusinessCheckException;

    /**
     * 根据条件搜索员工
     *
     * @param params 请求参数
     * @return
     * */
    List<MtStaff> queryStaffByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 根据手机号获取员工信息
     *
     * @param  mobile 手机
     * @throws BusinessCheckException
     * @return
     */
    MtStaff queryStaffByMobile(String mobile) throws BusinessCheckException;

    /**
     * 根据会员ID获取员工信息
     *
     * @param userId 会员ID
     * @throws BusinessCheckException
     * @return
     */
    MtStaff queryStaffByUserId(Integer userId) throws BusinessCheckException;

    /**
     * 根据手机号获取员工信息
     *
     * @param  mobile 手机
     * @throws BusinessCheckException
     * @return
     */
    StaffDto getStaffInfoByMobile(String mobile) throws BusinessCheckException;
}
