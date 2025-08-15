package com.fuint.module.clientApi.controller;

import com.fuint.common.dto.RegionDto;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.mapper.MtRegionMapper;
import com.fuint.repository.model.MtRegion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 省/市/区controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="会员端-省/市/区相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/clientApi/region")
public class ClientRegionController extends BaseController {

    private MtRegionMapper mtRegionMapper;

    /**
     * 获取地区树状结构
     */
    @ApiOperation(value = "获取地区树状结构")
    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject tree() {
        Map<String, Object> params = new HashMap<>();
        List<MtRegion> regionList = mtRegionMapper.selectByMap(params);

        List<RegionDto> treeData = new ArrayList<>();

        for (MtRegion mtRegion : regionList) {
             if (mtRegion.getLevel().equals(1)) {
                 RegionDto dto = new RegionDto();
                 dto.setId(mtRegion.getId());
                 dto.setName(mtRegion.getName());
                 dto.setCode(mtRegion.getCode());
                 dto.setPid(mtRegion.getPid());
                 dto.setLevel(mtRegion.getLevel() + "");
                 dto.setCity(new ArrayList<>());
                 treeData.add(dto);
             }
        }

        for (int i = 0; i < treeData.size(); i++) {
            List<RegionDto> cityArr = new ArrayList<>();
            for (MtRegion mtRegion : regionList) {
                if (treeData.get(i).getId().equals(mtRegion.getPid())) {
                    RegionDto dto = new RegionDto();
                    dto.setId(mtRegion.getId());
                    dto.setName(mtRegion.getName());
                    dto.setCode(mtRegion.getCode());
                    dto.setPid(mtRegion.getPid());
                    dto.setLevel(mtRegion.getLevel() + "");
                    List<RegionDto> regionArr = new ArrayList<>();
                    for (MtRegion mtRegion1 : regionList) {
                        if (mtRegion.getId().equals(mtRegion1.getPid())) {
                            RegionDto dto1 = new RegionDto();
                            dto1.setId(mtRegion1.getId());
                            dto1.setName(mtRegion1.getName());
                            dto1.setCode(mtRegion1.getCode());
                            dto1.setPid(mtRegion1.getPid());
                            dto1.setLevel(mtRegion1.getLevel() + "");
                            regionArr.add(dto1);
                        }
                    }
                    dto.setRegion(regionArr);
                    cityArr.add(dto);
                }
            }

            treeData.get(i).setCity(cityArr);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", treeData);

        return getSuccessResult(result);
    }
}
