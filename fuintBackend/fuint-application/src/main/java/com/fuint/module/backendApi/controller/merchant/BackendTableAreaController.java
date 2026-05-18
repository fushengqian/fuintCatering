package com.fuint.module.backendApi.controller.merchant;

import com.fuint.common.dto.system.AccountInfo;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.param.StatusParam;
import com.fuint.common.param.TableAreaPage;
import com.fuint.common.service.StoreService;
import com.fuint.common.service.TableAreaService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtStore;
import com.fuint.repository.model.MtTableArea;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 桌码区域管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="管理端-桌码区域相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/tableArea")
public class BackendTableAreaController extends BaseController {

    /**
     * 桌码区域服务接口
     */
    private TableAreaService tableAreaService;

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 桌码区域列表查询
     */
    @ApiOperation(value = "桌码区域列表查询")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:list')")
    public ResponseObject list(@ModelAttribute TableAreaPage tableAreaPage) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        if (accountInfo.getMerchantId() != null && accountInfo.getMerchantId() > 0) {
            tableAreaPage.setMerchantId(accountInfo.getMerchantId());
        }
        if (accountInfo.getStoreId() != null && accountInfo.getStoreId() > 0) {
            tableAreaPage.setStoreId(accountInfo.getStoreId());
        }

        PaginationResponse<MtTableArea> paginationResponse = tableAreaService.queryTableAreaListByPagination(tableAreaPage);
        List<MtStore> storeList = storeService.getActiveStoreList(accountInfo.getMerchantId(), accountInfo.getStoreId(), null);

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);
        result.put("storeList", storeList);

        return getSuccessResult(result);
    }

    /**
     * 更新桌码区域状态
     */
    @ApiOperation(value = "更新桌码区域状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:edit')")
    public ResponseObject updateStatus(@RequestBody StatusParam params) throws BusinessCheckException {
        AccountInfo accountInfo = TokenUtil.getAccountInfo();

        MtTableArea mtTableArea = tableAreaService.queryTableAreaById(params.getId());
        if (mtTableArea == null) {
            return getFailureResult(201);
        }

        mtTableArea.setOperator(accountInfo.getAccountName());
        mtTableArea.setStatus(params.getStatus());
        tableAreaService.updateTableArea(mtTableArea, accountInfo);

        return getSuccessResult(true);
    }

    /**
     * 保存桌码区域
     */
    @ApiOperation(value = "保存桌码区域")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:add')")
    public ResponseObject saveHandler(@RequestBody Map<String, Object> params) throws BusinessCheckException {
        String id = params.get("id") == null ? "" : params.get("id").toString();
        String status = params.get("status") == null ? StatusEnum.ENABLED.getKey() : params.get("status").toString();
        String storeId = params.get("storeId") == null ? "0" : params.get("storeId").toString();

        AccountInfo accountInfo = TokenUtil.getAccountInfo();
        MtTableArea mtTableArea = new MtTableArea();
        mtTableArea.setOperator(accountInfo.getAccountName());
        mtTableArea.setStatus(status);
        mtTableArea.setStoreId(Integer.parseInt(storeId));
        mtTableArea.setMerchantId(accountInfo.getMerchantId());
        if (StringUtil.isNotEmpty(id)) {
            mtTableArea.setId(Integer.parseInt(id));
            tableAreaService.updateTableArea(mtTableArea, accountInfo);
        } else {
            tableAreaService.addTableArea(mtTableArea);
        }

        return getSuccessResult(true);
    }

    /**
     * 获取桌码区域详情
     */
    @ApiOperation(value = "获取桌码区域详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    @CrossOrigin
    @PreAuthorize("@pms.hasPermission('tableArea:list')")
    public ResponseObject info(@PathVariable("id") Integer id) throws BusinessCheckException {
        MtTableArea tableAreaInfo = tableAreaService.queryTableAreaById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("tableAreaInfo", tableAreaInfo);

        return getSuccessResult(result);
    }
}
