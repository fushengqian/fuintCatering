package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.domain.TreeSelect;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.AccountService;
import com.fuint.common.service.SourceService;
import com.fuint.common.vo.MetaVo;
import com.fuint.common.vo.RouterVo;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.repository.mapper.TDutySourceMapper;
import com.fuint.repository.mapper.TSourceMapper;
import com.fuint.repository.model.TDutySource;
import com.fuint.repository.model.TSource;
import com.fuint.common.domain.TreeNode;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.utils.ArrayUtil;
import com.fuint.utils.StringUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理接口实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class SourceServiceImpl extends ServiceImpl<TSourceMapper, TSource> implements SourceService {

    private TDutySourceMapper tDutySourceMapper;

    private TSourceMapper tSourceMapper;

    /**
     * 后台账户服务接口
     */
    private AccountService accountService;

    /**
     * 获取有效的菜单集合
     *
     * @param merchantId 商户ID
     * @param status
     * @return
     */
    @Override
    public List<TSource> getAvailableSources(Integer merchantId, String status) {
        return tSourceMapper.findByStatus(merchantId, status);
    }

    /**
     * 获取菜单的属性结构
     *
     * @param merchantId 商户ID
     * @param status 状态
     * @return
     */
    @Override
    public List<TreeNode> getSourceTree(Integer merchantId, String status) {
        List<TSource> tSources = getAvailableSources(merchantId, status);
        List<TreeNode> trees = new ArrayList<>();
        if (tSources != null && tSources.size() > 0) {
            TreeNode sourceTreeNode;
            for (TSource tSource : tSources) {
                sourceTreeNode = new TreeNode();
                sourceTreeNode.setName(tSource.getSourceName());
                sourceTreeNode.setId(tSource.getSourceId());
                sourceTreeNode.setLevel(tSource.getSourceLevel());
                sourceTreeNode.setSort((tSource.getSourceStyle() == null || StringUtil.isEmpty(tSource.getSourceStyle())) ? 0 : Integer.parseInt(tSource.getSourceStyle()));
                sourceTreeNode.setPath(tSource.getPath());
                sourceTreeNode.setIcon(tSource.getNewIcon());
                sourceTreeNode.setIsMenu(tSource.getIsMenu());
                sourceTreeNode.setStatus(tSource.getStatus());
                sourceTreeNode.setPerms(tSource.getPath().replaceAll("/", ":"));
                if (tSource.getParentId() != null) {
                    sourceTreeNode.setPId(tSource.getParentId());
                } else {
                    sourceTreeNode.setPId(0);
                }
                trees.add(sourceTreeNode);
            }
        }
        return trees;
    }

    /**
     * 根据菜单ID集合查询菜单列表信息
     *
     * @param ids 菜单ID
     * @return
     */
    @Override
    public List<TSource> findDatasByIds(String[] ids) {
        Long[] arrays = new Long[ids.length];
        for (int i = 0; i < ids.length; i++) {
             arrays[i] = Long.parseLong(ids[i]);
        }
        return tSourceMapper.findByIdIn(ArrayUtil.toList(arrays));
    }

    /**
     * 根据账号ID获取菜单列表
     *
     * @param  merchantId 商户ID
     * @param  accountId 账号ID
     * @throws BusinessCheckException
     */
    @Override
    public List<TSource> getMenuListByUserId(Integer merchantId, Integer accountId) {
        if (merchantId == null) {
            merchantId = 0;
        }
        List<TSource> sourceList = tSourceMapper.findSourcesByAccountId(merchantId, accountId);
        return delRepeated(sourceList);
    }

    /**
     * 构建前端路由所需要的菜单
     *
     * @param treeNodes 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<TreeNode> treeNodes) {
        List<RouterVo> routers = new LinkedList<>();

        for (TreeNode menu : treeNodes) {
            RouterVo router = new RouterVo();
            if (menu.getIsMenu() == 0) {
                router.setHidden(true);
            } else {
                router.setHidden(false);
            }
            router.setName(menu.getEname());
            if (menu.getLevel() == 1) {
                router.setComponent("Layout");
                router.setPath("/" + menu.getEname().toLowerCase());
                router.setRedirect("noRedirect");
                router.setAlwaysShow(true);
            } else {
                if (menu.getIsMenu() == 2) {
                    router.setAlwaysShow(true);
                } else {
                    router.setAlwaysShow(false);
                }
                router.setComponent(menu.getPath());
                router.setPath('/' + menu.getPath());
            }
            router.setMeta(new MetaVo(menu.getName(), menu.getNewIcon(), false, null));
            List<TreeNode> cMenus = menu.getChildrens();
            if (cMenus != null && !cMenus.isEmpty() && cMenus.size() > 0) {
                router.setChildren(buildMenus(cMenus));
            }
            routers.add(router);
        }

        return routers;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<TreeNode> menus) {
        List<TreeNode> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 构建前端所需要树结构
     *
     * @param menus 菜单列表
     * @return 树结构列表
     */
    @Override
    public List<TreeNode> buildMenuTree(List<TreeNode> menus) {
        List<TreeNode> returnList = new ArrayList<TreeNode>();
        List<Long> tempList = new ArrayList<Long>();
        for (TreeNode dept : menus) {
            tempList.add(dept.getId());
        }
        for (Iterator<TreeNode> iterator = menus.iterator(); iterator.hasNext();) {
            TreeNode menu = (TreeNode) iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(menu.getPId())) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * 新增后台菜单
     *
     * @param tSource
     * @param accountId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "新增后台菜单")
    public void addSource(TSource tSource, Integer accountId) {
        this.save(tSource);
        // 赋访问权限
        List<Integer> dutyIds = accountService.getDutyIdsByAccountId(accountId);
        if (dutyIds != null && dutyIds.size() > 0) {
            for (Integer dutyId : dutyIds) {
                TDutySource dutySource = new TDutySource();
                dutySource.setDutyId(dutyId);
                dutySource.setSourceId(tSource.getSourceId());
                tDutySourceMapper.insert(dutySource);
            }
        }
    }

    /**
     * 修改后台菜单
     *
     * @param source
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "修改后台菜单")
    public void editSource(TSource source) {
        // 禁用或删除菜单处理
        if (source.getStatus().equals(StatusEnum.FORBIDDEN.getKey()) || source.getStatus().equals(StatusEnum.DISABLE.getKey())) {
            deleteSource(source, source.getStatus());
        } else {
            tSourceMapper.updateById(source);
        }
    }

    /**
     * 删除或禁用菜单
     *
     * @param source
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "删除后台菜单")
    public void deleteSource(TSource source, String status) {
        if (StringUtil.isEmpty(status)) {
            status = StatusEnum.DISABLE.getKey();
        }
        source.setStatus(status);
        tSourceMapper.updateById(source);

        Map<String, Object> param = new HashMap<>();
        param.put("STATUS", StatusEnum.ENABLED.getKey());
        param.put("PARENT_ID", source.getSourceId());
        List<TSource> dataList = tSourceMapper.selectByMap(param);
        if (dataList != null && dataList.size() > 0) {
            for (TSource tSource : dataList) {
                 deleteSource(tSource, status);
            }
        }
    }

    /**
     * 菜单去重
     *
     * @param sources 菜单列表
     * @return
     */
    private List<TSource> delRepeated(List<TSource> sources) {
        List<TSource> distinct = new ArrayList<>();
        if (sources != null) {
            Map<Long, Boolean> sourceMap = new HashMap<>();
            for (TSource tSource : sources) {
                if (sourceMap.get(tSource.getSourceId()) == null) {
                    sourceMap.put(tSource.getSourceId().longValue(), true);
                    distinct.add(tSource);
                }
            }
        }
        return distinct;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<TreeNode> list, TreeNode t) {
        // 得到子节点列表
        List<TreeNode> childList = getChildList(list, t);
        t.setChildrens(childList);
        for (TreeNode tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     *
     * @param list 菜单列表
     * @param t 当前节点
     * @return
     */
    private List<TreeNode> getChildList(List<TreeNode> list, TreeNode t) {
        List<TreeNode> tList = new ArrayList<TreeNode>();
        Iterator<TreeNode> it = list.iterator();
        while (it.hasNext()) {
            TreeNode n = it.next();
            if (n.getPId() == t.getId()) {
                tList.add(n);
            }
        }
        return tList;
    }

    /**
     * 判断是否有子节点
     *
     * @param list 菜单列表
     * @param t 当前节点
     * @return
     */
    private boolean hasChild(List<TreeNode> list, TreeNode t) {
        return getChildList(list, t).size() > 0;
    }
}
