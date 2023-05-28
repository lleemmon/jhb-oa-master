package com.jhb.auth.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhb.auth.mapper.SysRoleMenuMapper;
import com.jhb.auth.utils.MenuHelper;
import com.jhb.common.config.exception.GuiguException;
import com.jhb.model.system.SysMenu;
import com.jhb.auth.mapper.SysMenuMapper;
import com.jhb.auth.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhb.model.system.SysRoleMenu;
import com.jhb.vo.system.AssginMenuVo;
import com.jhb.vo.system.MetaVo;
import com.jhb.vo.system.RouterVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author jhb
 * @since 2023-03-21
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> findNodes() {
        List<SysMenu> sysMenus = baseMapper.selectList(null);
        return MenuHelper.buildTree(sysMenus);
    }

    @Override
    public void removeMenuById(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0){
            throw new GuiguException(201, "不能删除该菜单, 他有子菜单!");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<SysMenu> findSysMenuByRoleId(Long roleId) {
        //1 查询所有菜单- 添加条件 status=1
        LambdaQueryWrapper<SysMenu> wrapperSysMenu = new LambdaQueryWrapper<>();
        wrapperSysMenu.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrapperSysMenu);

        //2 根据角色id roleId查询 角色菜单关系表里面 角色id对应所有的菜单id
        LambdaQueryWrapper<SysRoleMenu> wrapperSysRoleMenu = new LambdaQueryWrapper<>();
        wrapperSysRoleMenu.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuMapper.selectList(wrapperSysRoleMenu);

        //3 根据获取菜单id，获取对应菜单对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());

        //3.1 拿着菜单id 和所有菜单集合里面id进行比较，如果相同封装
        allSysMenuList.stream().forEach(item -> {
            if(menuIdList.contains(item.getId())) {
                item.setSelect(true);
            } else {
                item.setSelect(false);
            }
        });

        //4 返回规定树形显示格式菜单列表
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }

    @Override
    @Transactional
    public void doAssign(AssginMenuVo assginMenuVo) {
        //删除角色已经分配的菜单
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, assginMenuVo.getRoleId());
        sysRoleMenuMapper.delete(wrapper);
        SysRoleMenu sysRoleMenu = new SysRoleMenu();
        sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        menuIdList.forEach(item -> {
            sysRoleMenu.setMenuId(item);
            sysRoleMenuMapper.insert(sysRoleMenu);
        });
    }

    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        //如果 userId = 1 代表管理员 查询所有
        //如果不是管理员 根据userId查询可以操作菜单的列表
        //涉及多表关联
        List<SysMenu> sysMenuList;
        //2、那查询出来的数据列表构建成框架要求的路由数据结构
        if(userId == 1){
            LambdaQueryWrapper<SysMenu>wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else{
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }
        //使用菜单操作工具类构建树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        return this.buildRouter(sysMenuTreeList);
    }

    @Override
    public List<String> findUserPermsListByUserId(Long userId) {
        //如果 userId = 1 代表管理员 查询所有
        //如果不是管理员 根据userId查询可以操作按钮的列表
        //从查询出来的数据获取到按钮值进行返回
        List<SysMenu> sysMenuList;
        //2、那查询出来的数据列表构建成框架要求的路由数据结构
        if(userId == 1){
            LambdaQueryWrapper<SysMenu>wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            sysMenuList = baseMapper.selectList(wrapper);
        }else{
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }
        //使用菜单操作工具类构建树形结构
        return sysMenuList.stream().filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());
    }

    private List<RouterVo> buildRouter(List<SysMenu> sysMenuTreeList) {
        List<RouterVo> routerVoList = new ArrayList<>();
        // type 0 顶层 type1 子层 type2 按钮
        sysMenuTreeList.forEach(menu -> {
            RouterVo routerVo = new RouterVo();
            routerVo.setHidden(false);
            routerVo.setAlwaysShow(false);
            routerVo.setPath(getRouterPath(menu));
            routerVo.setComponent(menu.getComponent());
            routerVo.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            //下一层数据部分
            List<SysMenu> children = menu.getChildren();
            if(menu.getType() == 1){
                //加载出来下面的隐藏路由
                List<SysMenu> hiddenMenuList = children.stream().filter(item -> StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                hiddenMenuList.forEach(hiddenMenu -> {
                    RouterVo routerHiddenVo = new RouterVo();
                    routerHiddenVo.setHidden(true);
                    routerHiddenVo.setAlwaysShow(false);
                    routerHiddenVo.setPath(getRouterPath(menu));
                    routerHiddenVo.setComponent(menu.getComponent());
                    routerHiddenVo.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
                    routerVoList.add(routerHiddenVo);
                });
            }else{
                if(!CollectionUtils.isEmpty(children)){
                    if(children.size() > 0){
                        routerVo.setAlwaysShow(true);
                    }
                    //递归
                    routerVo.setChildren(buildRouter(children));
                }
            }
            routerVoList.add(routerVo);
        });
        return routerVoList;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
}
