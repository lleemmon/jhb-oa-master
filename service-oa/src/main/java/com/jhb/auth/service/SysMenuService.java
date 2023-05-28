package com.jhb.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhb.model.system.SysMenu;
import com.jhb.vo.system.AssginMenuVo;
import com.jhb.vo.system.RouterVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author jhb
 * @since 2023-03-21
 */
public interface SysMenuService extends IService<SysMenu> {
    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    List<SysMenu> findSysMenuByRoleId(Long roleId);

    void doAssign(AssginMenuVo assginMenuVo);

    List<RouterVo> findUserMenuListByUserId(Long userId);

    List<String> findUserPermsListByUserId(Long userId);
}
