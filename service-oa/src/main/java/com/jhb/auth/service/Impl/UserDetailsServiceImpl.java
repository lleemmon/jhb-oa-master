package com.jhb.auth.service.Impl;

import com.jhb.auth.service.SysMenuService;
import com.jhb.auth.service.SysUserService;
import com.jhb.common.config.exception.GuiguException;
import com.jhb.model.system.SysUser;
import com.jhb.security.custom.CustomUser;
import com.jhb.security.custom.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysMenuService sysMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getByUsername(username);
        if(null == sysUser){
            throw new UsernameNotFoundException("用户名不存在!");
        }
        if(sysUser.getStatus() == 0){
            throw new GuiguException(201, "账号已停用");
        }
        //根据用户id得到操作权限数据
        List<String> userPermsLists = sysMenuService.findUserPermsListByUserId(sysUser.getId());
        List<SimpleGrantedAuthority>authorities = new ArrayList<>();
        userPermsLists.forEach(perm->{
            authorities.add(new SimpleGrantedAuthority(perm.trim()));
        });
        return new CustomUser(sysUser, authorities);
    }
}
