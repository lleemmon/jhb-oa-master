package com.jhb.auth.utils;

import com.jhb.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuHelper {
    public static List<SysMenu> buildTree(List<SysMenu> sysMenus) {
        List<SysMenu> trees = new ArrayList<>();
        Optional<SysMenu> first = sysMenus.stream().filter(item -> item.getParentId() == 0).findFirst();
        first.ifPresent(sysMenu -> trees.add(getChildren(sysMenu, sysMenus)));
        return sysMenus;
    }

    public static SysMenu getChildren(SysMenu sysMenu,
                                      List<SysMenu> sysMenuList) {
        sysMenu.setChildren(new ArrayList<SysMenu>());
        for(SysMenu item: sysMenuList){
            if(sysMenu.getId().longValue() == item.getParentId()){
                if(sysMenu.getChildren() == null) {
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(item);
            }
        }
        return sysMenu;
    }
}
