package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.rider.RiderDto;
import com.fuint.common.service.RiderService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.mapper.MtRiderMapper;
import com.fuint.repository.mapper.MtUserMapper;
import com.fuint.repository.model.MtRider;
import com.fuint.repository.model.MtUser;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * 骑手服务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class RiderServiceImpl extends ServiceImpl<MtRiderMapper, MtRider> implements RiderService {

    private static final Logger logger = LoggerFactory.getLogger(RiderServiceImpl.class);

    private MtRiderMapper mtRiderMapper;

    private MtUserMapper mtUserMapper;

    @Override
    public RiderDto getRiderByUserId(Integer userId) {
        MtRider rider = mtRiderMapper.findByUserId(userId);
        if (rider == null) {
            return null;
        }
        return convertToDto(rider);
    }

    @Override
    public RiderDto getRiderById(Integer riderId) {
        MtRider rider = mtRiderMapper.selectById(riderId);
        if (rider == null) {
            return null;
        }
        return convertToDto(rider);
    }

    @Override
    public boolean isRider(Integer userId) {
        MtUser user = mtUserMapper.selectById(userId);
        return user != null && "Y".equals(user.getIsRider());
    }

    @Override
    public RiderDto updateRider(RiderDto riderDto) throws BusinessCheckException {
        MtRider rider = mtRiderMapper.selectById(riderDto.getId());
        if (rider == null) {
            throw new BusinessCheckException("骑手信息不存在");
        }
        if (riderDto.getAvatar() != null) {
            // 同步更新会员头像
            MtUser user = mtUserMapper.selectById(rider.getUserId());
            if (user != null) {
                user.setAvatar(riderDto.getAvatar());
                mtUserMapper.updateById(user);
            }
        }
        return convertToDto(rider);
    }

    /**
     * 将 MtRider 转换为 RiderDto
     */
    private RiderDto convertToDto(MtRider rider) {
        RiderDto dto = new RiderDto();
        BeanUtils.copyProperties(rider, dto);

        // 查询会员信息
        MtUser user = mtUserMapper.selectById(rider.getUserId());
        if (user != null) {
            dto.setName(user.getName());
            dto.setMobile(user.getMobile());
            dto.setAvatar(user.getAvatar());
        }

        return dto;
    }
}
