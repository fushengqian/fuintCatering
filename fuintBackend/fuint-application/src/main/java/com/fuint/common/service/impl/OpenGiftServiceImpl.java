package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.Constants;
import com.fuint.common.dto.OpenGiftDto;
import com.fuint.common.enums.MessageEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.param.CouponReceiveParam;
import com.fuint.common.service.*;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.mapper.MtOpenGiftMapper;
import com.fuint.repository.mapper.MtUserMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * 开卡赠礼接口实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class OpenGiftServiceImpl extends ServiceImpl<MtOpenGiftMapper, MtOpenGift> implements OpenGiftService {

    private MtOpenGiftMapper mtOpenGiftMapper;

    private MtUserMapper mtUserMapper;

    /**
     * 卡券服务接口
     * */
    private CouponService couponService;

    /**
     * 会员等级服务接口
     * */
    private UserGradeService userGradeService;

    /**
     * 会员积分服务接口
     * */
    private PointService pointService;

    /**
     * 系统消息服务接口
     * */
    private MessageService messageService;

    /**
     * 获取开卡赠礼列表
     * @param  paramMap
     * @throws BusinessCheckException
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseObject getOpenGiftList(Map<String, Object> paramMap) throws BusinessCheckException {
        Integer pageNumber = paramMap.get("pageNumber") == null ? Constants.PAGE_NUMBER : Integer.parseInt(paramMap.get("pageNumber").toString());
        Integer pageSize = paramMap.get("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(paramMap.get("pageSize").toString());

        Page<MtOpenGift> pageHelper = PageHelper.startPage(pageNumber, pageSize);
        LambdaQueryWrapper<MtOpenGift> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtOpenGift::getStatus, StatusEnum.DISABLE.getKey());
        String merchantId = paramMap.get("merchantId") == null ? "" : paramMap.get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtOpenGift::getMerchantId, merchantId);
        }
        String couponId = paramMap.get("couponId") == null ? "" : paramMap.get("couponId").toString();
        if (StringUtils.isNotBlank(couponId)) {
            lambdaQueryWrapper.eq(MtOpenGift::getCouponId, couponId);
        }
        String gradeId = paramMap.get("gradeId") == null ? "" : paramMap.get("gradeId").toString();
        if (StringUtils.isNotBlank(gradeId)) {
            lambdaQueryWrapper.eq(MtOpenGift::getGradeId, Integer.parseInt(gradeId));
        }
        String status = paramMap.get("status") == null ? "" : paramMap.get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtOpenGift::getStatus, status);
        }

        lambdaQueryWrapper.orderByDesc(MtOpenGift::getId);
        List<MtOpenGift> openGiftList = mtOpenGiftMapper.selectList(lambdaQueryWrapper);
        List<OpenGiftDto> dataList = new ArrayList<>();
        for (MtOpenGift item : openGiftList) {
            OpenGiftDto dto = dealDetail(item);
            dataList.add(dto);
        }

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<OpenGiftDto> paginationResponse = new PaginationResponse(pageImpl, OpenGiftDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return new ResponseObject(200, "", paginationResponse);
    }

    /**
     * 新增开卡赠礼
     *
     * @param  mtOpenGift 赠礼信息
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "新增开卡赠礼")
    public MtOpenGift addOpenGift(MtOpenGift mtOpenGift) throws BusinessCheckException {
        mtOpenGift.setUpdateTime(new Date());
        mtOpenGift.setCreateTime(new Date());

        if (mtOpenGift.getCouponNum() != null && mtOpenGift.getCouponNum() > 100) {
            throw new BusinessCheckException("开卡赠礼卡券数量不能大于100");
        }

        this.save(mtOpenGift);
        return mtOpenGift;
    }

    /**
     * 根据ID获取开卡赠礼详情
     *
     * @param  id 开卡赠礼ID
     * @throws BusinessCheckException
     * @return
     */
    @Override
    public OpenGiftDto getOpenGiftDetail(Integer id) throws BusinessCheckException {
        MtOpenGift openGift = mtOpenGiftMapper.selectById(id);
        return dealDetail(openGift);
    }

    /**
     * 根据ID删除数据
     *
     * @param  id 开卡赠礼ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @OperationServiceLog(description = "删除开卡赠礼")
    public void deleteOpenGift(Integer id, String operator) {
        MtOpenGift MtOpenGift = mtOpenGiftMapper.selectById(id);
        if (null == MtOpenGift) {
            return;
        }

        MtOpenGift.setStatus(StatusEnum.DISABLE.getKey());
        MtOpenGift.setUpdateTime(new Date());

        mtOpenGiftMapper.updateById(MtOpenGift);
    }

    /**
     * 更新开卡赠礼
     *
     * @param  reqDto 实体参数
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新开卡赠礼")
    public MtOpenGift updateOpenGift(MtOpenGift reqDto) throws BusinessCheckException {
        MtOpenGift mtOpenGift = mtOpenGiftMapper.selectById(reqDto.getId());
        if (mtOpenGift == null) {
            throw new BusinessCheckException("该数据状态异常");
        }

        mtOpenGift.setId(reqDto.getId());
        mtOpenGift.setUpdateTime(new Date());

        if (null != reqDto.getOperator()) {
            mtOpenGift.setOperator(reqDto.getOperator());
        }

        if (null != reqDto.getStatus()) {
            mtOpenGift.setStatus(reqDto.getStatus());
        }

        if (null != reqDto.getCouponId()) {
            mtOpenGift.setCouponId(reqDto.getCouponId());
        }

        if (null != reqDto.getGradeId()) {
            mtOpenGift.setGradeId(reqDto.getGradeId());
        }

        if (null != reqDto.getPoint()) {
            mtOpenGift.setPoint(reqDto.getPoint());
        }

        if (null != reqDto.getCouponNum()) {
            if (reqDto.getCouponNum() > 100) {
                throw new BusinessCheckException("开卡赠礼卡券数量不能大于100");
            }
            mtOpenGift.setCouponNum(reqDto.getCouponNum());
        }

        mtOpenGiftMapper.updateById(mtOpenGift);
        return mtOpenGift;
    }

    /**
     * 开卡赠礼
     *
     * @param userId 会员ID
     * @param gradeId 等级ID
     * @return
     * */
    @Override
    public Boolean openGift(Integer userId, Integer gradeId, boolean isNewMember) throws BusinessCheckException {
        if (gradeId == null || gradeId.compareTo(0) <= 0) {
            return false;
        }
        MtUser user = mtUserMapper.selectById(userId);
        if (user.getIsStaff().equals(YesOrNoEnum.YES.getKey())) {
            return false;
        }
        if (user == null) {
            throw new BusinessCheckException("会员状态异常");
        }
        if (user.getGradeId() == null) {
            user.setGradeId(0);
        }
        MtUserGrade oldGrade = userGradeService.queryUserGradeById(user.getMerchantId(), user.getGradeId(), user.getId());
        MtUserGrade gradeInfo = userGradeService.queryUserGradeById(user.getMerchantId(), gradeId, user.getId());
        // 设置有效期
        if (gradeInfo.getValidDay() >= 0) {
            user.setStartTime(new Date());
            Date endDate = new Date();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(endDate);
            calendar.add(calendar.DATE, gradeInfo.getValidDay());
            endDate = calendar.getTime();
            user.setEndTime(endDate);
            if (gradeInfo.getValidDay() == 0) {
                user.setStartTime(null);
                user.setEndTime(null);
            }
        }
        user.setGradeId(gradeId);
        user.setUpdateTime(new Date());
        mtUserMapper.updateById(user);
        // 会员往低了改变，没有开卡赠礼
        if (!isNewMember && oldGrade != null && oldGrade.getGrade() >= gradeInfo.getGrade()) {
            return false;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("grade_id", gradeId.toString());
        params.put("status", StatusEnum.ENABLED.getKey());
        params.put("merchant_id", user.getMerchantId());
        List<MtOpenGift> openGiftList = mtOpenGiftMapper.selectByMap(params);
        if (openGiftList.size() > 0) {
            Integer totalPoint = 0;
            BigDecimal totalAmount = new BigDecimal("0");
            for(MtOpenGift item : openGiftList) {
               // 加积分
               if (item.getPoint() > 0) {
                   MtPoint reqPointDto = new MtPoint();
                   reqPointDto.setUserId(userId);
                   reqPointDto.setAmount(item.getPoint());
                   reqPointDto.setDescription("开卡赠送"+ item.getPoint() +"积分");
                   reqPointDto.setOperator("系统");
                   pointService.addPoint(reqPointDto);
                   totalPoint = totalPoint + item.getPoint();
               }
               // 返卡券
               if (item.getCouponId() > 0) {
                   MtCoupon mtCoupon = couponService.queryCouponById(item.getCouponId());
                   if (mtCoupon != null && mtCoupon.getStatus().equals(StatusEnum.ENABLED.getKey())) {
                       CouponReceiveParam param = new CouponReceiveParam();
                       param.setCouponId(item.getCouponId());
                       param.setUserId(userId);
                       param.setNum(item.getCouponNum() == null ? 1 : item.getCouponNum());
                       String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                       couponService.sendCoupon(item.getCouponId(), userId, param.getNum(), true, uuid, "");
                       totalAmount = totalAmount.add(mtCoupon.getAmount());
                   }
               }
            }
            // 弹框消息
            MtMessage msg = new MtMessage();
            msg.setMerchantId(user.getMerchantId());
            msg.setType(MessageEnum.POP_MSG.getKey());
            msg.setUserId(userId);
            msg.setTitle("温馨提示");
            msg.setSendTime(new Date());
            msg.setIsSend(YesOrNoEnum.YES.getKey());
            msg.setParams("");
            if (totalAmount.compareTo(new BigDecimal("0")) > 0 && totalPoint > 0) {
                msg.setContent("系统赠送您价值￥" + totalAmount + "卡券和" + totalPoint + "积分，请注意查收！");
                messageService.addMessage(msg);
            } else if(totalAmount.compareTo(new BigDecimal("0")) > 0) {
                msg.setContent("系统赠送您价值" + totalAmount + "卡券，请注意查收！");
                messageService.addMessage(msg);
            } else if(totalPoint > 0) {
                msg.setContent("系统赠送您" + totalPoint + "积分，请注意查收！");
                messageService.addMessage(msg);
            }
        }
        return true;
    }

    /**
     * 赠礼详情
     *
     * @param  openGiftInfo 赠礼详情
     * @throws BusinessCheckException
     * @return OpenGiftDto
     * */
    private OpenGiftDto dealDetail(MtOpenGift openGiftInfo) throws BusinessCheckException {
        OpenGiftDto dto = new OpenGiftDto();

        dto.setId(openGiftInfo.getId());
        dto.setCreateTime(DateUtil.formatDate(openGiftInfo.getCreateTime(), "yyyy.MM.dd HH:mm"));
        dto.setUpdateTime(DateUtil.formatDate(openGiftInfo.getUpdateTime(), "yyyy.MM.dd HH:mm"));
        dto.setStatus(openGiftInfo.getStatus());
        dto.setCouponNum(openGiftInfo.getCouponNum());
        dto.setPoint(openGiftInfo.getPoint());
        dto.setOperator(openGiftInfo.getOperator());

        MtCoupon couponInfo = couponService.queryCouponById(openGiftInfo.getCouponId());
        dto.setCouponInfo(couponInfo);

        MtUserGrade gradeInfo = userGradeService.queryUserGradeById(openGiftInfo.getMerchantId(), openGiftInfo.getGradeId(), 0);
        dto.setGradeInfo(gradeInfo);

        return dto;
    }
}
